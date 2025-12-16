package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.SenderType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity ChatMessage - Tin nhắn chat
 */
@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_chat_msg_conv", columnList = "conversation_id"),
    @Index(name = "idx_chat_msg_sender", columnList = "sender_type, sender_id"),
    @Index(name = "idx_chat_msg_sent", columnList = "sent_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    private SenderType senderType;

    @Column(name = "sender_id", nullable = false)
    private Long senderId; // user_id hoặc staff_id

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "attachments", columnDefinition = "JSON")
    private String attachments; // Array of file URLs

    // AI Bot suggestion
    @Column(name = "is_bot_generated")
    @Builder.Default
    private Boolean isBotGenerated = false;

    @Column(name = "bot_confidence_score", precision = 3, scale = 2)
    private BigDecimal botConfidenceScore; // 0-1

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "sent_at")
    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();

    // Helper methods
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public boolean isFromUser() {
        return senderType == SenderType.USER;
    }

    public boolean isFromStaff() {
        return senderType == SenderType.STAFF;
    }

    public boolean isFromBot() {
        return senderType == SenderType.BOT;
    }
}
