package com.badmintonshop.dto.inventory;

import lombok.*;

/**
 * Request DTO for inventory adjustment
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryAdjustRequest {

    private Long inventoryId;
    private Long productId;
    private Long variantId;
    private Integer adjustment; // Positive for increase, negative for decrease
    private String reason;
    private String referenceNumber; // e.g., PO number, order ID
}
