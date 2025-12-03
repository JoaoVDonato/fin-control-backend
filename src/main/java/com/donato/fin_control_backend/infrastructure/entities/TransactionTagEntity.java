package com.donato.fin_control_backend.infrastructure.entities;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transaction_tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionTagEntity {

    @EmbeddedId
    private TransactionTagId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("transactionId")
    @JoinColumn(name = "transaction_id", nullable = false)
    private TransactionEntity transaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id", nullable = false)
    private TagEntity tag;
}
