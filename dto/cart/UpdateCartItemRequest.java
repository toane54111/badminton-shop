package com.badmintonshop.dto.cart;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for updating cart item quantity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCartItemRequest {
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 99, message = "Quantity cannot exceed 99")
    private Integer quantity;
}
