package com.badmintonshop.dto.inventory;

import com.badmintonshop.entity.Inventory;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for Inventory
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryDTO {

    private Long inventoryId;
    private Long productId;
    private String productName;
    private String productSku;
    private Long variantId;
    private String variantName;
    private String warehouseLocation;
    private Integer quantityAvailable;
    private Integer quantityReserved;
    private Integer quantitySold;
    private Integer lowStockThreshold;
    private Integer reorderPoint;
    private Boolean isLowStock;
    private Boolean isOutOfStock;
    private LocalDateTime lastRestockDate;
    private Integer lastRestockQuantity;
    private LocalDateTime updatedAt;

    public static InventoryDTO fromEntity(Inventory inventory) {
        if (inventory == null)
            return null;

        InventoryDTOBuilder builder = InventoryDTO.builder()
                .inventoryId(inventory.getInventoryId())
                .warehouseLocation(inventory.getWarehouseLocation())
                .quantityAvailable(inventory.getQuantityAvailable())
                .quantityReserved(inventory.getQuantityReserved())
                .quantitySold(inventory.getQuantitySold())
                .lowStockThreshold(inventory.getLowStockThreshold())
                .reorderPoint(inventory.getReorderPoint())
                .isLowStock(inventory.isLowStock())
                .isOutOfStock(inventory.isOutOfStock())
                .lastRestockDate(inventory.getLastRestockDate())
                .lastRestockQuantity(inventory.getLastRestockQuantity())
                .updatedAt(inventory.getUpdatedAt());

        if (inventory.getProduct() != null) {
            builder.productId(inventory.getProduct().getProductId())
                    .productName(inventory.getProduct().getName())
                    .productSku(inventory.getProduct().getSku());
        }

        if (inventory.getVariant() != null) {
            builder.variantId(inventory.getVariant().getVariantId())
                    .variantName(inventory.getVariant().getVariantName());
        }

        return builder.build();
    }
}
