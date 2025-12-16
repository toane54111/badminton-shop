package com.badmintonshop.entity.enums;

public enum PaymentStatus {
    PENDING,        // Chờ thanh toán
    PAID,           // Đã thanh toán
    FAILED,         // Thanh toán thất bại
    REFUNDED,       // Đã hoàn tiền
    PARTIAL_REFUND  // Hoàn tiền một phần
}
