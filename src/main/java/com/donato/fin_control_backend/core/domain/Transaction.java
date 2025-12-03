package com.donato.fin_control_backend.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    // Identificador da transação
    private Long id;
    // Usuário responsável pela movimentação
    private User user;
    // Conta onde o lançamento aconteceu
    private Account account;
    // Categoria associada ao gasto/receita
    private Category category;
    // Data em que ocorreu a transação
    private LocalDate date;
    // Tipo (ENTRADA ou SAIDA)
    private String type;
    // Descrição detalhada inserida pelo usuário
    private String description;
    // Valor movimentado
    private BigDecimal amount;
    // Status de pagamento (PAGO, PENDENTE, etc.)
    private String status;
    // Indica se pertence a uma compra parcelada
    private boolean installment;
    // Quantidade total de parcelas
    private Integer installmentTotal;
    // Número da parcela atual
    private Integer installmentIndex;
    // Data de criação do registro
    private LocalDateTime createdAt;
    // Conjunto de tags para classificação livre
    private Set<Tag> tags;
}
