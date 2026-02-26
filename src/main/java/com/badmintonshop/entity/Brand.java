package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.BrandStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity Brand - Thương hiệu: Yonex, Victor, Lining, Mizuno...
 */
@Entity
@Table(name = "brands", indexes = {
    @Index(name = "idx_brands_active", columnList = "is_active"),
    @Index(name = "idx_brands_status", columnList = "status"),
    @Index(name = "idx_brands_order", columnList = "display_order"),
    @Index(name = "idx_brands_deleted", columnList = "deleted_at"),
    @Index(name = "idx_brands_active_deleted_order", columnList = "is_active, deleted_at, display_order")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
public class Brand extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "country", length = 100)
    private String country; // VD: Japan, China, Taiwan

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private BrandStatus status = BrandStatus.ACTIVE;

    // Relationships
    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL)
    @Builder.Default
    private List<StringProduct> strings = new ArrayList<>();
}
