package com.badmintonshop.dto.request;

import com.badmintonshop.entity.enums.StaffStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating staff status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusRequest {

    @NotNull(message = "Status is required")
    private StaffStatus status;
}
