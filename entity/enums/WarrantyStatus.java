package com.badmintonshop.entity.enums;

public enum WarrantyStatus {
    REQUESTED,                  // Đã yêu cầu
    APPROVED,                   // Đã duyệt
    REJECTED,                   // Đã từ chối
    SENT_TO_MANUFACTURER,       // Đã gửi nhà sản xuất
    RETURNED_FROM_MANUFACTURER, // Đã nhận lại từ nhà sản xuất
    COMPLETED                   // Hoàn thành
}
