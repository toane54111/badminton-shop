package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.PaymentMethodType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity PaymentMethodConfig - Cấu hình phương thức thanh toán
 */
@Entity
@Table(name = "payment_methods_config", indexes = {
    @Index(name = "idx_payment_methods_type", columnList = "type"),
    @Index(name = "idx_payment_methods_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "method_id")
    private Long methodId;

    @Column(name = "name", nullable = false, length = 100)
    private String name; // VD: MoMo

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code; // MOMO

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PaymentMethodType type;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Configuration (encrypted)
    @Column(name = "config", columnDefinition = "JSON")
    private String config; // API keys, merchant_id, etc

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;
}
