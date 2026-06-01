package com.donato.fin_control_backend.core.usecases;

import com.donato.fin_control_backend.adapters.api.http.handler.BusinessRuleException;
import com.donato.fin_control_backend.adapters.api.http.handler.ResourceNotFoundException;
import com.donato.fin_control_backend.core.domain.Tag;
import com.donato.fin_control_backend.core.domain.Transaction;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.ports.inbound.TransactionService;
import com.donato.fin_control_backend.core.ports.outbound.*;
import com.donato.fin_control_backend.core.usecases.commands.CreateTransactionCommand;
import com.donato.fin_control_backend.core.usecases.commands.UpdateTransactionCommand;
import com.donato.fin_control_backend.infrastructure.entities.*;
import com.donato.fin_control_backend.infrastructure.mappers.TagMapper;
import com.donato.fin_control_backend.infrastructure.mappers.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private static final String STATUS_PAID = "PAID";
    private static final String TYPE_INCOME = "INCOME";
    private static final String TYPE_EXPENSE = "EXPENSE";
    private static final String TYPE_TRANSFER = "TRANSFER";

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public List<Transaction> createTransaction(CreateTransactionCommand command, User user) {
        UserEntity userEntity = resolveUser(user);

        if (TYPE_TRANSFER.equals(command.getType())) {
            return createTransfer(command, userEntity);
        }

        if (command.isInstallment() && command.getInstallmentTotal() != null && command.getInstallmentTotal() > 1) {
            return createInstallments(command, userEntity);
        }

        TransactionEntity tx = buildTransaction(command, userEntity, null);
        TransactionEntity saved = transactionRepository.save(tx);
        recalculateBalance(saved.getAccount());
        Set<Tag> tags = resolveTags(command.getTagIds(), userEntity.getId());
        return List.of(TransactionMapper.toDomain(saved, tags));
    }

    @Override
    @Transactional
    public Transaction updateTransaction(Long id, UpdateTransactionCommand command, User user) {
        UserEntity userEntity = resolveUser(user);
        TransactionEntity tx = transactionRepository.findByIdAndUserId(id, userEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (tx.getTransferPeer() != null) {
            throw new BusinessRuleException("Transfer transactions cannot be edited directly. Delete and recreate.");
        }

        AccountEntity previousAccount = tx.getAccount();
        String previousStatus = tx.getStatus();

        if (command.getAccountId() != null) {
            AccountEntity newAccount = accountRepository.findByIdAndUserId(command.getAccountId(), userEntity.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
            tx.setAccount(newAccount);
        }
        if (command.getAmount() != null) tx.setAmount(command.getAmount());
        if (command.getDate() != null) tx.setDate(command.getDate());
        if (command.getSettlementDate() != null) tx.setSettlementDate(command.getSettlementDate());
        if (command.getDescription() != null) tx.setDescription(command.getDescription());
        if (command.getNotes() != null) tx.setNotes(command.getNotes());
        if (command.getStatus() != null) tx.setStatus(command.getStatus());

        if (command.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findByIdAndUserId(command.getCategoryId(), userEntity.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            validateCategoryType(tx.getType(), category.getType());
            tx.setCategory(category);
        }

        TransactionEntity saved = transactionRepository.save(tx);

        // recalculate both accounts if account changed
        recalculateBalance(previousAccount);
        if (!previousAccount.getId().equals(saved.getAccount().getId())) {
            recalculateBalance(saved.getAccount());
        }

        Set<Tag> tags = resolveTags(command.getTagIds(), userEntity.getId());
        return TransactionMapper.toDomain(saved, tags);
    }

    @Override
    public Transaction getTransaction(Long id, User user) {
        UserEntity userEntity = resolveUser(user);
        TransactionEntity tx = transactionRepository.findByIdAndUserId(id, userEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        return TransactionMapper.toDomain(tx, Collections.emptySet());
    }

    @Override
    public Page<Transaction> listTransactions(User user, Long accountId, Long categoryId, String type,
                                              String status, LocalDate dateFrom, LocalDate dateTo,
                                              String search, Pageable pageable) {
        UserEntity userEntity = resolveUser(user);
        return transactionRepository.findByFilters(
                userEntity.getId(), accountId, categoryId, type, status,
                dateFrom, dateTo, search, pageable)
                .map(tx -> TransactionMapper.toDomain(tx, Collections.emptySet()));
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id, User user) {
        UserEntity userEntity = resolveUser(user);
        TransactionEntity tx = transactionRepository.findByIdAndUserId(id, userEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        AccountEntity account = tx.getAccount();
        AccountEntity peerAccount = null;

        // collect peer account before deleting anything
        if (tx.getTransferPeer() != null) {
            peerAccount = tx.getTransferPeer().getAccount();
            // unlink peers first so FK constraints are satisfied, then delete both
            TransactionEntity peer = tx.getTransferPeer();
            tx.setTransferPeer(null);
            peer.setTransferPeer(null);
            transactionRepository.save(tx);
            transactionRepository.save(peer);
            transactionRepository.delete(peer);
        }

        transactionRepository.delete(tx);

        // recalculate after both records are gone
        if (account != null) recalculateBalance(account);
        if (peerAccount != null && (account == null || !peerAccount.getId().equals(account.getId()))) {
            recalculateBalance(peerAccount);
        }
    }

    @Override
    @Transactional
    public Transaction updateStatus(Long id, String status, User user) {
        UserEntity userEntity = resolveUser(user);
        TransactionEntity tx = transactionRepository.findByIdAndUserId(id, userEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        tx.setStatus(status);
        TransactionEntity saved = transactionRepository.save(tx);
        recalculateBalance(saved.getAccount());
        return TransactionMapper.toDomain(saved, Collections.emptySet());
    }

    // --- private helpers ---

    private List<Transaction> createTransfer(CreateTransactionCommand command, UserEntity userEntity) {
        if (command.getDestinationAccountId() == null) {
            throw new BusinessRuleException("Destination account is required for transfers");
        }
        if (command.getAccountId().equals(command.getDestinationAccountId())) {
            throw new BusinessRuleException("Source and destination accounts must be different");
        }

        AccountEntity source = accountRepository.findByIdAndUserId(command.getAccountId(), userEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found"));
        AccountEntity destination = accountRepository.findByIdAndUserId(command.getDestinationAccountId(), userEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));

        LocalDateTime now = LocalDateTime.now();

        TransactionEntity outTx = TransactionEntity.builder()
                .user(userEntity)
                .account(source)
                .date(command.getDate())
                .settlementDate(command.getSettlementDate())
                .type(TYPE_EXPENSE)
                .description(command.getDescription() != null ? command.getDescription() : "Transferência")
                .notes(command.getNotes())
                .amount(command.getAmount())
                .status(command.getStatus())
                .installment(false)
                .createdAt(now)
                .build();

        TransactionEntity inTx = TransactionEntity.builder()
                .user(userEntity)
                .account(destination)
                .date(command.getDate())
                .settlementDate(command.getSettlementDate())
                .type(TYPE_INCOME)
                .description(command.getDescription() != null ? command.getDescription() : "Transferência")
                .notes(command.getNotes())
                .amount(command.getAmount())
                .status(command.getStatus())
                .installment(false)
                .createdAt(now)
                .build();

        TransactionEntity savedOut = transactionRepository.save(outTx);
        TransactionEntity savedIn = transactionRepository.save(inTx);

        // link peers
        savedOut.setTransferPeer(savedIn);
        savedIn.setTransferPeer(savedOut);
        transactionRepository.save(savedOut);
        transactionRepository.save(savedIn);

        recalculateBalance(source);
        recalculateBalance(destination);

        return List.of(
                TransactionMapper.toDomain(savedOut, Collections.emptySet()),
                TransactionMapper.toDomain(savedIn, Collections.emptySet()));
    }

    private List<Transaction> createInstallments(CreateTransactionCommand command, UserEntity userEntity) {
        AccountEntity account = accountRepository.findByIdAndUserId(command.getAccountId(), userEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        CategoryEntity category = resolveCategory(command.getCategoryId(), userEntity.getId(), command.getType());

        int total = command.getInstallmentTotal();
        BigDecimal installmentAmount = command.getAmount().divide(BigDecimal.valueOf(total), 2, java.math.RoundingMode.HALF_UP);
        LocalDate baseDate = command.getDate();
        LocalDateTime now = LocalDateTime.now();
        List<TransactionEntity> saved = new ArrayList<>();

        for (int i = 1; i <= total; i++) {
            TransactionEntity tx = TransactionEntity.builder()
                    .user(userEntity)
                    .account(account)
                    .category(category)
                    .date(baseDate.plusMonths(i - 1))
                    .type(command.getType())
                    .description(command.getDescription())
                    .notes(command.getNotes())
                    .amount(installmentAmount)
                    .status(command.getStatus())
                    .installment(true)
                    .installmentTotal(total)
                    .installmentIndex(i)
                    .createdAt(now)
                    .build();
            saved.add(transactionRepository.save(tx));
        }

        recalculateBalance(account);
        return saved.stream()
                .map(tx -> TransactionMapper.toDomain(tx, Collections.emptySet()))
                .collect(Collectors.toList());
    }

    private TransactionEntity buildTransaction(CreateTransactionCommand command,
                                               UserEntity userEntity,
                                               Integer installmentIndex) {
        AccountEntity account = accountRepository.findByIdAndUserId(command.getAccountId(), userEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        CategoryEntity category = resolveCategory(command.getCategoryId(), userEntity.getId(), command.getType());

        return TransactionEntity.builder()
                .user(userEntity)
                .account(account)
                .category(category)
                .date(command.getDate())
                .settlementDate(command.getSettlementDate())
                .type(command.getType())
                .description(command.getDescription())
                .notes(command.getNotes())
                .amount(command.getAmount())
                .status(command.getStatus())
                .installment(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private CategoryEntity resolveCategory(Long categoryId, Long userId, String txType) {
        if (categoryId == null) return null;
        CategoryEntity category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        validateCategoryType(txType, category.getType());
        return category;
    }

    private void validateCategoryType(String txType, String categoryType) {
        if (TYPE_INCOME.equals(txType) && TYPE_EXPENSE.equals(categoryType)) {
            throw new BusinessRuleException("Cannot use an EXPENSE category for an INCOME transaction");
        }
        if (TYPE_EXPENSE.equals(txType) && TYPE_INCOME.equals(categoryType)) {
            throw new BusinessRuleException("Cannot use an INCOME category for an EXPENSE transaction");
        }
    }

    private void recalculateBalance(AccountEntity account) {
        if (account == null) return;
        BigDecimal offset = transactionRepository.calculateBalanceOffset(account.getId());
        account.setBalance(account.getInitialBalance().add(offset));
        accountRepository.save(account);
    }

    private Set<Tag> resolveTags(Set<Long> tagIds, Long userId) {
        if (tagIds == null || tagIds.isEmpty()) return Collections.emptySet();
        return tagIds.stream()
                .map(tagId -> tagRepository.findByIdAndUserId(tagId, userId)
                        .map(TagMapper::toDomain)
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private UserEntity resolveUser(User user) {
        if (user.getId() != null) {
            return userRepository.findById(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        return userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
