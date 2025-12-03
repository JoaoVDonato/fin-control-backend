package com.donato.fin_control_backend.infrastructure.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionTagId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "tag_id")
    private Long tagId;
}
