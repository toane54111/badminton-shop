package com.badmintonshop.exception;

import lombok.Getter;

@Getter
public class InsufficientStockException extends RuntimeException {
    private final Long productId;
    private final int requestedQuantity;
    private final int availableQuantity;
    
    public InsufficientStockException(Long productId, int requestedQuantity, int availableQuantity) {
        super(String.format("Sản phẩm không đủ số lượng. Yêu cầu: %d, Còn lại: %d", 
                requestedQuantity, availableQuantity));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
}
