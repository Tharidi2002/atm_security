package com.atm.alert.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Alert message is required")
    @Size(max = 1000)
    @Column(name = "message")
    private String message;

    @NotBlank(message = "Phone number is required")
    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private AlertCategory category;

    @Column(name = "bank_name")
    @Size(max = 100)
    private String bankName;

    @Column(name = "location")
    @Size(max = 500)
    private String location;

    @Column(name = "is_acknowledged")
    private Boolean isAcknowledged = false;

    @Column(name = "acknowledged_by")
    @Size(max = 100)
    private String acknowledgedBy;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "incident_details")
    @Size(max = 2000)
    private String incidentDetails;

    public Alert() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Alert(String message, String phoneNumber) {
        this();
        this.message = message;
        this.phoneNumber = phoneNumber;
        this.severity = categorizeSeverity(message);
        this.category = categorizeCategory(message);
    }

    private AlertSeverity categorizeSeverity(String message) {
        String upperMessage = message.toUpperCase();
        if (upperMessage.contains("FIRE") || upperMessage.contains("TAMPER") || 
            upperMessage.contains("EMERGENCY") || upperMessage.contains("CRITICAL")) {
            return AlertSeverity.CRITICAL;
        } else if (upperMessage.contains("WARNING") || upperMessage.contains("FAULT") || 
                   upperMessage.contains("ERROR")) {
            return AlertSeverity.WARNING;
        } else {
            return AlertSeverity.INFO;
        }
    }

    private AlertCategory categorizeCategory(String message) {
        String upperMessage = message.toUpperCase();
        if (upperMessage.contains("FIRE")) {
            return AlertCategory.FIRE_ALARM;
        } else if (upperMessage.contains("DOOR") || upperMessage.contains("DOOR OPEN")) {
            return AlertCategory.DOOR_OPEN;
        } else if (upperMessage.contains("POWER") || upperMessage.contains("ELECTRICITY")) {
            return AlertCategory.POWER_FAILURE;
        } else if (upperMessage.contains("TAMPER")) {
            return AlertCategory.PHYSICAL_TAMPERING;
        } else if (upperMessage.contains("CASH") || upperMessage.contains("MONEY")) {
            return AlertCategory.CASH_THEFT;
        } else if (upperMessage.contains("NETWORK") || upperMessage.contains("CONNECTIVITY")) {
            return AlertCategory.NETWORK_ISSUE;
        } else {
            return AlertCategory.OTHER;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { 
        this.message = message; 
        this.severity = categorizeSeverity(message);
        this.category = categorizeCategory(message);
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }

    public AlertCategory getCategory() { return category; }
    public void setCategory(AlertCategory category) { this.category = category; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Boolean getIsAcknowledged() { return isAcknowledged; }
    public void setIsAcknowledged(Boolean isAcknowledged) { this.isAcknowledged = isAcknowledged; }

    public String getAcknowledgedBy() { return acknowledgedBy; }
    public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }

    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getIncidentDetails() { return incidentDetails; }
    public void setIncidentDetails(String incidentDetails) { this.incidentDetails = incidentDetails; }
}
