package com.badmintonshop.dto.product;

import com.badmintonshop.entity.enums.ProductStatus;
import com.badmintonshop.entity.enums.ProductType;
import com.badmintonshop.entity.enums.RacketFlexibility;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating/updating Product
 * Field names match the actual Product entity
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 200, message = "Tên sản phẩm tối đa 200 ký tự")
    private String name;

    private String slug;

    @NotBlank(message = "SKU không được để trống")
    @Size(max = 100, message = "SKU tối đa 100 ký tự")
    private String sku;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    private Long brandId;

    @NotNull(message = "Loại sản phẩm không được để trống")
    private ProductType productType;

    @Size(max = 500, message = "Mô tả ngắn tối đa 500 ký tự")
    private String shortDescription;

    private String fullDescription;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0", message = "Giá phải lớn hơn hoặc bằng 0")
    private BigDecimal basePrice;

    @DecimalMin(value = "0", message = "Giá so sánh phải lớn hơn hoặc bằng 0")
    private BigDecimal compareAtPrice;

    @DecimalMin(value = "0", message = "Giá gốc phải lớn hơn hoặc bằng 0")
    private BigDecimal costPrice;

    @DecimalMin(value = "0", message = "Trọng lượng phải lớn hơn hoặc bằng 0")
    private BigDecimal weight;

    private String dimensions;

    // Racket specific fields (matching entity)
    private RacketFlexibility racketFlexibility;
    private String racketBalancePoint;
    private String racketShaftDiameter;
    private String racketFrameShape;
    private String racketRecommendedTension;
    private String racketMaxTension;
    private String racketMaterial;

    // Status
    private ProductStatus status;
    private Boolean isFeatured;
    private Boolean isNewArrival;
    private Boolean isBestSeller;
    private Integer displayOrder;

    // SEO
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;

    // Images (for create/update)
    private List<String> imageUrls;
    private String primaryImageUrl;
}
