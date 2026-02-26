package com.badmintonshop.exception;

/**
 * Exception thrown when a setting is not found
 */
public class SettingNotFoundException extends RuntimeException {

    public SettingNotFoundException(String settingKey) {
        super("Setting not found: " + settingKey);
    }

    public SettingNotFoundException(Long id) {
        super("Setting not found with id: " + id);
    }
}
