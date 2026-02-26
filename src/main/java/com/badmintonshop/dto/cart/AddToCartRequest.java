package com.badmintonshop.dto.cart;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for adding item to cart
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddToCartRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    private Long variantId;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    @Builder.Default
    private Integer quantity = 1;
    
    // Stringing service options (for rackets)
    private Long stringingServiceId;
    private Long stringProductId;
    private java.math.BigDecimal tension;
    private String stringingNotes;
}
