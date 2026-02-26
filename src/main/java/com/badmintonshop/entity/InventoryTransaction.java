package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity InventoryTransaction - Lịch sử xuất nhập kho
 */
@Entity
@Table(name = "inventory_transactions", indexes = {
    @Index(name = "idx_inv_trans_product", columnList = "product_id"),
    @Index(name = "idx_inv_trans_variant", columnList = "variant_id"),
    @Index(name = "idx_inv_trans_type", columnList = "transaction_type"),
    @Index(name = "idx_inv_trans_ref", columnList = "reference_type, reference_id"),
    @Index(name = "idx_inv_trans_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "reason")
    private String reason;

    @Column(name = "reference_type", length = 50)
    private String referenceType; // order/purchase_order/adjustment

    @Column(name = "reference_id")
    private Long referenceId; // ID của order hoặc purchase order

    @Column(name = "quantity_before")
    private Integer quantityBefore;

    @Column(name = "quantity_after")
    private Integer quantityAfter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Staff createdByStaff;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
