package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.EventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity UserBehaviorEvent - Events người dùng - cho analytics
 */
@Entity
@Table(name = "user_behavior_events", indexes = {
    @Index(name = "idx_behavior_events_user", columnList = "user_id"),
    @Index(name = "idx_behavior_events_session", columnList = "session_id"),
    @Index(name = "idx_behavior_events_type", columnList = "event_type"),
    @Index(name = "idx_behavior_events_product", columnList = "product_id"),
    @Index(name = "idx_behavior_events_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBehaviorEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    // Event data
    @Column(name = "page_url", length = 500)
    private String pageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @Column(name = "event_data", columnDefinition = "JSON")
    private String eventData; // Additional event metadata

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
