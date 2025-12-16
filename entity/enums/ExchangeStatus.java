package com.badmintonshop.entity.enums;

public enum ExchangeStatus {
    REQUESTED,         // Đã yêu cầu
    APPROVED,          // Đã duyệt
    REJECTED,          // Đã từ chối
    PICKUP_SCHEDULED,  // Đã lên lịch lấy hàng
    RECEIVED,          // Đã nhận hàng cũ
    PROCESSING,        // Đang xử lý
    NEW_ITEM_SENT,     // Đã gửi hàng mới
    COMPLETED,         // Hoàn thành
    CANCELLED          // Đã hủy
}
