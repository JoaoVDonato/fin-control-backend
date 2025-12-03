package com.donato.fin_control_backend.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionTag {

    // Transação associada
    private Transaction transaction;
    // Tag vinculada à transação
    private Tag tag;
}
