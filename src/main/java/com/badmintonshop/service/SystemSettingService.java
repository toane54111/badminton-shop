package com.badmintonshop.service;

import com.badmintonshop.dto.response.SystemSettingResponse;
import com.badmintonshop.entity.Staff;
import com.badmintonshop.entity.SystemSetting;
import com.badmintonshop.exception.SettingNotFoundException;
import com.badmintonshop.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing system settings
 * Uses Caffeine cache for performance
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SystemSettingService {

    private final SystemSettingRepository settingRepository;

    /**
     * Get all settings
     */
    @Cacheable(value = "systemSettings", key = "'all'")
    public List<SystemSettingResponse> getAllSettings() {
        log.debug("Fetching all system settings from database");
        return settingRepository.findAll().stream()
                .map(SystemSettingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get setting by key
     */
    @Cacheable(value = "systemSettings", key = "#key")
    public SystemSettingResponse getByKey(String key) {
        log.debug("Fetching setting by key: {}", key);
        return settingRepository.findBySettingKey(key)
                .map(SystemSettingResponse::fromEntity)
                .orElseThrow(() -> new SettingNotFoundException(key));
    }

    /**
     * Get public settings only (for frontend)
     */
    @Cacheable(value = "systemSettings", key = "'public'")
    public Map<String, String> getPublicSettings() {
        log.debug("Fetching public settings from database");
        return settingRepository.findByIsPublicTrue().stream()
                .collect(Collectors.toMap(
                        SystemSetting::getSettingKey,
                        SystemSetting::getSettingValue
                ));
    }

    /**
     * Get setting value by key with default value
     */
    public String getValue(String key, String defaultValue) {
        return settingRepository.getValueByKey(key).orElse(defaultValue);
    }

    /**
     * Get setting value as Integer
     */
    public Integer getIntValue(String key, Integer defaultValue) {
        return settingRepository.findBySettingKey(key)
                .map(SystemSetting::getIntValue)
                .orElse(defaultValue);
    }

    /**
     * Get setting value as Boolean
     */
    public Boolean getBooleanValue(String key, Boolean defaultValue) {
        return settingRepository.findBySettingKey(key)
                .map(SystemSetting::getBooleanValue)
                .orElse(defaultValue);
    }

    /**
     * Update multiple settings at once
     */
    @Transactional
    @CacheEvict(value = "systemSettings", allEntries = true)
    public Map<String, SystemSettingResponse> updateSettings(Map<String, String> settings, Staff updatedBy) {
        log.info("Updating {} settings by staff: {}", settings.size(), 
                updatedBy != null ? updatedBy.getEmail() : "system");
        
        Map<String, SystemSettingResponse> results = new HashMap<>();
        
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            SystemSetting setting = settingRepository.findBySettingKey(key)
                    .orElseThrow(() -> new SettingNotFoundException(key));
            
            setting.setSettingValue(value);
            setting.setUpdatedByStaff(updatedBy);
            setting.setUpdatedAt(LocalDateTime.now());
            
            SystemSetting saved = settingRepository.save(setting);
            results.put(key, SystemSettingResponse.fromEntity(saved));
        }
        
        log.info("Successfully updated {} settings", results.size());
        return results;
    }

    /**
     * Update single setting
     */
    @Transactional
    @CacheEvict(value = "systemSettings", allEntries = true)
    public SystemSettingResponse updateSetting(String key, String value, Staff updatedBy) {
        log.info("Updating setting: {} by staff: {}", key, 
                updatedBy != null ? updatedBy.getEmail() : "system");
        
        SystemSetting setting = settingRepository.findBySettingKey(key)
                .orElseThrow(() -> new SettingNotFoundException(key));
        
        setting.setSettingValue(value);
        setting.setUpdatedByStaff(updatedBy);
        setting.setUpdatedAt(LocalDateTime.now());
        
        return SystemSettingResponse.fromEntity(settingRepository.save(setting));
    }

    /**
     * Check if setting exists
     */
    public boolean exists(String key) {
        return settingRepository.existsBySettingKey(key);
    }

    /**
     * Get settings as key-value map (for template rendering)
     */
    @Cacheable(value = "systemSettings", key = "'map'")
    public Map<String, String> getSettingsAsMap() {
        return settingRepository.findAll().stream()
                .collect(Collectors.toMap(
                        SystemSetting::getSettingKey,
                        SystemSetting::getSettingValue
                ));
    }
}
