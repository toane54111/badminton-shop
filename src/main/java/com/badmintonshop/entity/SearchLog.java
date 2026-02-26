package com.badmintonshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity SearchLog - Lịch sử tìm kiếm - phân tích hành vi
 */
@Entity
@Table(name = "search_logs", indexes = {
    @Index(name = "idx_search_logs_user", columnList = "user_id"),
    @Index(name = "idx_search_logs_query", columnList = "query"),
    @Index(name = "idx_search_logs_clicked", columnList = "clicked_product_id"),
    @Index(name = "idx_search_logs_searched", columnList = "searched_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_id")
    private Long searchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "query", nullable = false)
    private String query;

    @Column(name = "results_count", nullable = false)
    private Integer resultsCount;

    // Filters applied
    @Column(name = "filters", columnDefinition = "JSON")
    private String filters; // Category, brand, price range...

    // Click tracking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clicked_product_id")
    private Product clickedProduct;

    @Column(name = "clicked_position")
    private Integer clickedPosition; // Vị trí sản phẩm được click trong kết quả

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "searched_at")
    @Builder.Default
    private LocalDateTime searchedAt = LocalDateTime.now();

    // Helper method
    public void recordClick(Product product, int position) {
        this.clickedProduct = product;
        this.clickedPosition = position;
    }
}
