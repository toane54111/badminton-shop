package com.badmintonshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base Entity với các trường audit và soft delete
 * Tất cả entity khác nên extends class này
 * 
 * Note: created_by và updated_by được quản lý riêng trong từng entity
 * vì schema DB sử dụng FK tới bảng staff thay vì username String
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Soft delete timestamp
     * NULL = active, NOT NULL = deleted
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Check if entity is soft deleted
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Soft delete the entity
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restore soft deleted entity
     */
    public void restore() {
        this.deletedAt = null;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
