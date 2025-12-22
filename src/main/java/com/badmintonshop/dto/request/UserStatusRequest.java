package com.badmintonshop.dto.request;

import com.badmintonshop.entity.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating user status (ban/unban)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusRequest {

    @NotNull(message = "Status is required")
    private UserStatus status;
    
    private String reason; // Reason for ban/lock
}
