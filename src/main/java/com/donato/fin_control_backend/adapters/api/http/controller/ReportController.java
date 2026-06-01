package com.donato.fin_control_backend.adapters.api.http.controller;

import com.donato.fin_control_backend.adapters.api.http.dto.ReportDTO;
import com.donato.fin_control_backend.adapters.api.http.handler.ResourceNotFoundException;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.ports.inbound.UserService;
import com.donato.fin_control_backend.core.ports.outbound.ReportRepository;
import com.donato.fin_control_backend.core.ports.outbound.UserRepository;
import com.donato.fin_control_backend.infrastructure.entities.TransactionEntity;
import com.donato.fin_control_backend.infrastructure.entities.UserEntity;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/finControl/reports")
public class ReportController {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/monthly")
    public ResponseEntity<List<ReportDTO.MonthlyReportEntry>> monthly(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestHeader("Authorization") String auth) {

        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));
        List<Object[]> rows = reportRepository.monthlyReport(user.getId(), dateFrom, dateTo);

        record MonthKey(int year, int month) {}
        Map<MonthKey, BigDecimal[]> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            int y = ((Number) row[0]).intValue();
            int m = ((Number) row[1]).intValue();
            String type = (String) row[2];
            BigDecimal amount = (BigDecimal) row[3];
            MonthKey key = new MonthKey(y, m);
            map.computeIfAbsent(key, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if ("INCOME".equals(type)) map.get(key)[0] = amount;
            else map.get(key)[1] = amount;
        }

        List<ReportDTO.MonthlyReportEntry> result = map.entrySet().stream().map(e -> {
            int y = e.getKey().year();
            int m = e.getKey().month();
            BigDecimal inc = e.getValue()[0], exp = e.getValue()[1];
            String label = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-BR")) + "/" + y;
            return ReportDTO.MonthlyReportEntry.builder()
                    .year(y).month(m).label(label)
                    .income(inc).expense(exp).net(inc.subtract(exp))
                    .build();
        }).toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/by-category")
    public ResponseEntity<List<ReportDTO.CategoryReportEntry>> byCategory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestHeader("Authorization") String auth) {

        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));
        List<Object[]> rows = reportRepository.reportByCategory(user.getId(), dateFrom, dateTo);

        List<ReportDTO.CategoryReportEntry> result = rows.stream().map(r ->
                ReportDTO.CategoryReportEntry.builder()
                        .categoryId(((Number) r[0]).longValue())
                        .categoryName((String) r[1])
                        .type((String) r[2])
                        .total((BigDecimal) r[3])
                        .count(((Number) r[4]).longValue())
                        .build()
        ).toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/by-account")
    public ResponseEntity<List<ReportDTO.AccountReportEntry>> byAccount(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestHeader("Authorization") String auth) {

        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));
        List<Object[]> rows = reportRepository.reportByAccount(user.getId(), dateFrom, dateTo);

        Map<Long, BigDecimal[]> map = new LinkedHashMap<>();
        Map<Long, String> names = new LinkedHashMap<>();
        for (Object[] r : rows) {
            Long accId = ((Number) r[0]).longValue();
            String type = (String) r[2];
            BigDecimal amount = (BigDecimal) r[3];
            map.computeIfAbsent(accId, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            names.put(accId, (String) r[1]);
            if ("INCOME".equals(type)) map.get(accId)[0] = amount;
            else map.get(accId)[1] = amount;
        }

        List<ReportDTO.AccountReportEntry> result = map.entrySet().stream().map(e -> {
            BigDecimal inc = e.getValue()[0], exp = e.getValue()[1];
            return ReportDTO.AccountReportEntry.builder()
                    .accountId(e.getKey()).accountName(names.get(e.getKey()))
                    .income(inc).expense(exp).net(inc.subtract(exp))
                    .build();
        }).toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/export")
    public void exportCsv(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestHeader("Authorization") String auth,
            HttpServletResponse response) throws IOException {

        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));
        List<TransactionEntity> txs = reportRepository.findAllForExport(
                user.getId(), accountId, categoryId, type, status, dateFrom, dateTo);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"transactions.csv\"");

        try (PrintWriter w = response.getWriter()) {
            w.println("id,date,type,description,amount,status,account,category");
            for (TransactionEntity tx : txs) {
                w.printf("%d,%s,%s,\"%s\",%s,%s,\"%s\",\"%s\"%n",
                        tx.getId(),
                        tx.getDate(),
                        tx.getType(),
                        tx.getDescription() != null ? tx.getDescription().replace("\"", "\"\"") : "",
                        tx.getAmount(),
                        tx.getStatus(),
                        tx.getAccount() != null ? tx.getAccount().getName() : "",
                        tx.getCategory() != null ? tx.getCategory().getName() : "");
            }
        }
    }

    private String extractToken(String auth) {
        return auth.startsWith("Bearer ") ? auth.substring(7) : auth.trim();
    }

    private UserEntity resolve(User user) {
        if (user.getId() != null) {
            return userRepository.findById(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        return userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
