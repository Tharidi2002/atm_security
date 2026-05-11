package com.atm.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Size(max = 100)
    @Column(name = "username")
    private String username;

    @NotBlank(message = "Action is required")
    @Size(max = 100)
    @Column(name = "action")
    private String action;

    @Size(max = 50)
    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Size(max = 45)
    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Size(max = 50)
    @Column(name = "module")
    private String module;

    @Column(name = "status")
    @Size(max = 20)
    private String status = "SUCCESS";

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "session_id")
    @Size(max = 100)
    private String sessionId;

    @Column(name = "request_id")
    @Size(max = 100)
    private String requestId;

    @Column(name = "correlation_id")
    @Size(max = 100)
    private String correlationId;

    public AuditLog() {
        this.createdAt = LocalDateTime.now();
    }

    public AuditLog(Long userId, String username, String action, String entityType, Long entityId) {
        this();
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public AuditLog(Long userId, String username, String action, String entityType, Long entityId, 
                   String oldValue, String newValue, String ipAddress, String userAgent) {
        this(userId, username, action, entityType, entityId);
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    // Action constants
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_VIEW = "VIEW";
    public static final String ACTION_ACKNOWLEDGE = "ACKNOWLEDGE";
    public static final String ACTION_ASSIGN = "ASSIGN";
    public static final String ACTION_RESOLVE = "RESOLVE";
    public static final String ACTION_ESCALATE = "ESCALATE";
    public static final String ACTION_EXPORT = "EXPORT";
    public static final String ACTION_IMPORT = "IMPORT";
    public static final String ACTION_PASSWORD_CHANGE = "PASSWORD_CHANGE";
    public static final String ACTION_2FA_ENABLE = "2FA_ENABLE";
    public static final String ACTION_2FA_DISABLE = "2FA_DISABLE";
    public static final String ACTION_ACCOUNT_LOCK = "ACCOUNT_LOCK";
    public static final String ACTION_ACCOUNT_UNLOCK = "ACCOUNT_UNLOCK";

    // Entity types
    public static final String ENTITY_USER = "USER";
    public static final String ENTITY_ALERT = "ALERT";
    public static final String ENTITY_INCIDENT = "INCIDENT";
    public static final String ENTITY_STATION = "STATION";
    public static final String ENTITY_BANK = "BANK";
    public static final String ENTITY_REPORT = "REPORT";
    public static final String ENTITY_NOTIFICATION = "NOTIFICATION";
    public static final String ENTITY_ROLE = "ROLE";
    public static final String ENTITY_AUDIT_LOG = "AUDIT_LOG";

    // Status values
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_PARTIAL = "PARTIAL";
}
