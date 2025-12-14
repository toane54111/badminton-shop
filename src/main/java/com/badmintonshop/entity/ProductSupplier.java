package com.badmintonshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entity ProductSupplier - 1 sản phẩm có thể nhập từ nhiều nhà cung cấp khác nhau
 */
@Entity
@Table(name = "product_suppliers", indexes = {
    @Index(name = "idx_product_suppliers_product", columnList = "product_id"),
    @Index(name = "idx_product_suppliers_supplier", columnList = "supplier_id"),
    @Index(name = "idx_product_suppliers_preferred", columnList = "is_preferred")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_product_supplier", columnNames = {"product_id", "supplier_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSupplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_supplier_id")
    private Long productSupplierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "supplier_sku", length = 100)
    private String supplierSku; // Mã SP bên nhà cung cấp

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice; // Giá nhập từ NCC này

    @Column(name = "lead_time_days")
    private Integer leadTimeDays; // Thời gian giao hàng (ngày)

    @Column(name = "min_order_quantity")
    @Builder.Default
    private Integer minOrderQuantity = 1; // Số lượng đặt tối thiểu

    @Column(name = "is_preferred")
    @Builder.Default
    private Boolean isPreferred = false; // NCC ưu tiên cho SP này
}
