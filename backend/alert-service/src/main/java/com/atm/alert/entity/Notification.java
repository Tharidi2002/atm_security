package com.atm.alert.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id")
    private Alert alert;

    @NotBlank(message = "Notification type is required")
    @Size(max = 50)
    @Column(name = "type")
    private String type;

    @NotBlank(message = "Notification message is required")
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Size(max = 50)
    @Column(name = "sent_via")
    private String sentVia;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "priority")
    private String priority = "MEDIUM";

    @Column(name = "action_required")
    private Boolean actionRequired = false;

    @Column(name = "action_url")
    @Size(max = 500)
    private String actionUrl;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.sentAt = LocalDateTime.now();
    }

    public Notification(Long userId, String type, String message, String priority) {
        this();
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.priority = priority;
    }

    public Notification(Long userId, Alert alert, String type, String message, String priority) {
        this();
        this.userId = userId;
        this.alert = alert;
        this.type = type;
        this.message = message;
        this.priority = priority;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Alert getAlert() { return alert; }
    public void setAlert(Alert alert) { this.alert = alert; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public String getSentVia() { return sentVia; }
    public void setSentVia(String sentVia) { this.sentVia = sentVia; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Boolean getActionRequired() { return actionRequired; }
    public void setActionRequired(Boolean actionRequired) { this.actionRequired = actionRequired; }

    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    // Helper methods
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean requiresAction() {
        return this.actionRequired != null && this.actionRequired && !this.isRead;
    }

    public void markAsSent(String sentVia) {
        this.sentVia = sentVia;
        this.sentAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Notification types
    public static final String TYPE_ALERT = "ALERT";
    public static final String TYPE_INCIDENT = "INCIDENT";
    public static final String TYPE_SYSTEM = "SYSTEM";
    public static final String TYPE_MAINTENANCE = "MAINTENANCE";
    public static final String TYPE_ESCALATION = "ESCALATION";
    public static final String TYPE_REPORT = "REPORT";

    // Priority levels
    public static final String PRIORITY_CRITICAL = "CRITICAL";
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_MEDIUM = "MEDIUM";
    public static final String PRIORITY_LOW = "LOW";

    // Sent via channels
    public static final String SENT_VIA_WEBSOCKET = "WEBSOCKET";
    public static final String SENT_VIA_EMAIL = "EMAIL";
    public static final String SENT_VIA_SMS = "SMS";
    public static final String SENT_VIA_PUSH = "PUSH";
}
