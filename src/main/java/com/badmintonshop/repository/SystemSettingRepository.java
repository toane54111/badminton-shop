package com.badmintonshop.repository;

import com.badmintonshop.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SystemSetting entity
 */
@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {

    /**
     * Find setting by key
     */
    Optional<SystemSetting> findBySettingKey(String settingKey);

    /**
     * Find all public settings
     */
    List<SystemSetting> findByIsPublicTrue();

    /**
     * Find settings by multiple keys
     */
    List<SystemSetting> findBySettingKeyIn(List<String> settingKeys);

    /**
     * Check if setting exists by key
     */
    boolean existsBySettingKey(String settingKey);

    /**
     * Get setting value by key
     */
    @Query("SELECT s.settingValue FROM SystemSetting s WHERE s.settingKey = :key")
    Optional<String> getValueByKey(@Param("key") String key);
}
