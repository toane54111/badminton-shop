package com.badmintonshop.entity;

import com.badmintonshop.entity.enums.ChatPriority;
import com.badmintonshop.entity.enums.ChatStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity ChatConversation - Cuộc trò chuyện support
 */
@Entity
@Table(name = "chat_conversations", indexes = {
    @Index(name = "idx_chat_conv_user", columnList = "user_id"),
    @Index(name = "idx_chat_conv_staff", columnList = "staff_id"),
    @Index(name = "idx_chat_conv_status", columnList = "status"),
    @Index(name = "idx_chat_conv_priority", columnList = "priority"),
    @Index(name = "idx_chat_conv_started", columnList = "started_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id")
    private Long conversationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private ChatStatus status = ChatStatus.ACTIVE;

    // Priority
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    @Builder.Default
    private ChatPriority priority = ChatPriority.MEDIUM;

    // Tags
    @Column(name = "tags", columnDefinition = "JSON")
    private String tags; // VD: ["stringing_advice", "product_inquiry"]

    @Column(name = "started_at")
    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "last_message_at")
    @Builder.Default
    private LocalDateTime lastMessageAt = LocalDateTime.now();

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    // Relationships
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sentAt ASC")
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    // Helper methods
    public void assignStaff(Staff staff) {
        this.staff = staff;
    }

    public void resolve() {
        this.status = ChatStatus.RESOLVED;
    }

    public void close() {
        this.status = ChatStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    public void updateLastMessageTime() {
        this.lastMessageAt = LocalDateTime.now();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        message.setConversation(this);
        updateLastMessageTime();
    }
}
