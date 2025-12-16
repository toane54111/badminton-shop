package com.badmintonshop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

/**
 * Entity FAQ - Câu hỏi thường gặp (dùng cho chatbot)
 */
@Entity
@Table(name = "faq", indexes = {
    @Index(name = "idx_faq_category", columnList = "category"),
    @Index(name = "idx_faq_active", columnList = "is_active"),
    @Index(name = "idx_faq_order", columnList = "display_order"),
    @Index(name = "idx_faq_view_count", columnList = "view_count"),
    @Index(name = "idx_faq_deleted", columnList = "deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
public class FAQ extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "faq_id")
    private Long faqId;

    @Column(name = "category", length = 100)
    private String category; // VD: Stringing, Product, Shipping

    @Column(name = "question", nullable = false, length = 500)
    private String question;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Helper methods
    public void incrementViewCount() {
        this.viewCount++;
    }
}
