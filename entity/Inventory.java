package com.badmintonshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity Inventory - Tồn kho theo sản phẩm và variant
 */
@Entity
@Table(name = "inventory", indexes = {
    @Index(name = "idx_inventory_product", columnList = "product_id"),
    @Index(name = "idx_inventory_variant", columnList = "variant_id"),
    @Index(name = "idx_inventory_location", columnList = "warehouse_location"),
    @Index(name = "idx_inventory_available", columnList = "quantity_available"),
    @Index(name = "idx_inventory_reorder", columnList = "quantity_available, reorder_point")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @Column(name = "warehouse_location", length = 100)
    @Builder.Default
    private String warehouseLocation = "main";

    @Column(name = "quantity_available", nullable = false)
    @Builder.Default
    private Integer quantityAvailable = 0;

    @Column(name = "quantity_reserved")
    @Builder.Default
    private Integer quantityReserved = 0; // Đang trong đơn hàng chưa xác nhận

    @Column(name = "quantity_sold")
    @Builder.Default
    private Integer quantitySold = 0;

    @Column(name = "low_stock_threshold")
    @Builder.Default
    private Integer lowStockThreshold = 10;

    @Column(name = "reorder_point")
    @Builder.Default
    private Integer reorderPoint = 20; // Mức tồn kho cần nhập thêm

    @Column(name = "last_restock_date")
    private LocalDateTime lastRestockDate;

    @Column(name = "last_restock_quantity")
    private Integer lastRestockQuantity;

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Helper methods
    public boolean isLowStock() {
        return quantityAvailable <= lowStockThreshold;
    }

    public boolean needsReorder() {
        return quantityAvailable <= reorderPoint;
    }

    public boolean isOutOfStock() {
        return quantityAvailable <= 0;
    }

    public int getActualAvailable() {
        return Math.max(0, quantityAvailable - quantityReserved);
    }

    public void reserve(int quantity) {
        if (quantity <= getActualAvailable()) {
            this.quantityReserved += quantity;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void releaseReservation(int quantity) {
        this.quantityReserved = Math.max(0, this.quantityReserved - quantity);
        this.updatedAt = LocalDateTime.now();
    }

    public void sell(int quantity) {
        this.quantityReserved = Math.max(0, this.quantityReserved - quantity);
        this.quantityAvailable = Math.max(0, this.quantityAvailable - quantity);
        this.quantitySold += quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void restock(int quantity) {
        this.quantityAvailable += quantity;
        this.lastRestockDate = LocalDateTime.now();
        this.lastRestockQuantity = quantity;
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
