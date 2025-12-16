package com.badmintonshop.entity.enums;

/**
 * User Status - Values must match MySQL ENUM exactly (UPPERCASE)
 */
public enum UserStatus {
    ACTIVE,      // Hoạt động bình thường
    BANNED,      // Cấm vĩnh viễn (vi phạm TOS)
    LOCKED       // Tạm khóa (nhập sai pass nhiều lần)
}
