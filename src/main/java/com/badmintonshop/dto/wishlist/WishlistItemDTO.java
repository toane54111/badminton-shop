package com.badmintonshop.dto.wishlist;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for wishlist item
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistItemDTO {
    
    private Long wishlistId;
    private Long productId;
    private String productName;
    private String productSlug;
    private String productImage;
    private BigDecimal basePrice;
    private BigDecimal compareAtPrice;
    private BigDecimal discountPercentage;
    private String brandName;
    private Boolean inStock;
    private String notes;
    private LocalDateTime addedAt;
}
