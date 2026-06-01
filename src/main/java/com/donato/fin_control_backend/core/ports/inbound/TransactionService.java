package com.donato.fin_control_backend.core.ports.inbound;

import com.donato.fin_control_backend.core.domain.Transaction;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.usecases.commands.CreateTransactionCommand;
import com.donato.fin_control_backend.core.usecases.commands.UpdateTransactionCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

    List<Transaction> createTransaction(CreateTransactionCommand command, User user);

    Transaction updateTransaction(Long id, UpdateTransactionCommand command, User user);

    Transaction getTransaction(Long id, User user);

    Page<Transaction> listTransactions(User user, Long accountId, Long categoryId, String type,
                                       String status, LocalDate dateFrom, LocalDate dateTo,
                                       String search, Pageable pageable);

    void deleteTransaction(Long id, User user);

    Transaction updateStatus(Long id, String status, User user);
}
