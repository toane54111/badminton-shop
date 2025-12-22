package com.badmintonshop.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for updating multiple settings at once
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettingsUpdateRequest {

    @NotEmpty(message = "Settings map cannot be empty")
    private Map<String, String> settings;
}
