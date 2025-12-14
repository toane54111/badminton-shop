package com.badmintonshop.repository;

import com.badmintonshop.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for EmailTemplate entity
 */
@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    /**
     * Find template by key
     */
    Optional<EmailTemplate> findByTemplateKey(String templateKey);

    /**
     * Find all active templates
     */
    List<EmailTemplate> findByIsActiveTrue();

    /**
     * Find all active templates ordered by name
     */
    List<EmailTemplate> findByIsActiveTrueOrderByNameAsc();

    /**
     * Check if template exists by key
     */
    boolean existsByTemplateKey(String templateKey);

    /**
     * Check if template exists by key excluding specific id
     */
    boolean existsByTemplateKeyAndTemplateIdNot(String templateKey, Long templateId);
}
