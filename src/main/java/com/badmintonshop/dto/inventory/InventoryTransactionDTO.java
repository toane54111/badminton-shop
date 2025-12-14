package com.badmintonshop.dto.inventory;

import com.badmintonshop.entity.InventoryTransaction;
import com.badmintonshop.entity.enums.TransactionType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for Inventory Transaction (history)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransactionDTO {

    private Long transactionId;
    private Long productId;
    private String productName;
    private String productSku;
    private Long variantId;
    private String variantName;
    private TransactionType transactionType;
    private Integer quantity;
    private Integer quantityBefore;
    private Integer quantityAfter;
    private String reason;
    private String referenceType;
    private Long referenceId;
    private LocalDateTime createdAt;

    public static InventoryTransactionDTO fromEntity(InventoryTransaction transaction) {
        if (transaction == null)
            return null;

        InventoryTransactionDTOBuilder builder = InventoryTransactionDTO.builder()
                .transactionId(transaction.getTransactionId())
                .transactionType(transaction.getTransactionType())
                .quantity(transaction.getQuantity())
                .quantityBefore(transaction.getQuantityBefore())
                .quantityAfter(transaction.getQuantityAfter())
                .reason(transaction.getReason())
                .referenceType(transaction.getReferenceType())
                .referenceId(transaction.getReferenceId())
                .createdAt(transaction.getCreatedAt());

        if (transaction.getProduct() != null) {
            builder.productId(transaction.getProduct().getProductId())
                    .productName(transaction.getProduct().getName())
                    .productSku(transaction.getProduct().getSku());
        }

        if (transaction.getVariant() != null) {
            builder.variantId(transaction.getVariant().getVariantId())
                    .variantName(transaction.getVariant().getVariantName());
        }

        return builder.build();
    }
}
