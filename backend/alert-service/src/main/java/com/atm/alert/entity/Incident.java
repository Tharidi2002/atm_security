package com.atm.alert.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", nullable = false)
    private Alert alert;

    @NotBlank(message = "Assigned team is required")
    @Size(max = 100)
    @Column(name = "assigned_team")
    private String assignedTeam;

    @Size(max = 50)
    @Column(name = "priority")
    private String priority = "MEDIUM";

    @Size(max = 50)
    @Column(name = "lifecycle_status")
    private String lifecycleStatus = "NEW";

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Size(max = 100)
    @Column(name = "resolved_by")
    private String resolvedBy;

    @Column(name = "time_to_respond_minutes")
    private Integer timeToRespondMinutes;

    @Column(name = "time_to_resolve_minutes")
    private Integer timeToResolveMinutes;

    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "investigating_at")
    private LocalDateTime investigatingAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    public Incident() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // Set SLA deadline based on priority
        this.slaDeadline = LocalDateTime.now().plusMinutes(120); // 2 hours default
    }

    public Incident(Alert alert, String assignedTeam, String priority) {
        this();
        this.alert = alert;
        this.assignedTeam = assignedTeam;
        this.priority = priority;
        
        // Set SLA based on priority
        if ("CRITICAL".equals(priority)) {
            this.slaDeadline = LocalDateTime.now().plusMinutes(30); // 30 minutes
        } else if ("HIGH".equals(priority)) {
            this.slaDeadline = LocalDateTime.now().plusMinutes(60); // 1 hour
        } else {
            this.slaDeadline = LocalDateTime.now().plusMinutes(120); // 2 hours
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Alert getAlert() { return alert; }
    public void setAlert(Alert alert) { this.alert = alert; }

    public String getAssignedTeam() { return assignedTeam; }
    public void setAssignedTeam(String assignedTeam) { this.assignedTeam = assignedTeam; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getLifecycleStatus() { return lifecycleStatus; }
    public void setLifecycleStatus(String lifecycleStatus) { this.lifecycleStatus = lifecycleStatus; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public Integer getTimeToRespondMinutes() { return timeToRespondMinutes; }
    public void setTimeToRespondMinutes(Integer timeToRespondMinutes) { this.timeToRespondMinutes = timeToRespondMinutes; }

    public Integer getTimeToResolveMinutes() { return timeToResolveMinutes; }
    public void setTimeToResolveMinutes(Integer timeToResolveMinutes) { this.timeToResolveMinutes = timeToResolveMinutes; }

    public LocalDateTime getSlaDeadline() { return slaDeadline; }
    public void setSlaDeadline(LocalDateTime slaDeadline) { this.slaDeadline = slaDeadline; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }

    public LocalDateTime getInvestigatingAt() { return investigatingAt; }
    public void setInvestigatingAt(LocalDateTime investigatingAt) { this.investigatingAt = investigatingAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    // Helper methods
    public void acknowledgeIncident() {
        this.lifecycleStatus = "ACKNOWLEDGED";
        this.acknowledgedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        if (this.createdAt != null) {
            this.timeToRespondMinutes = (int) java.time.Duration.between(this.createdAt, this.acknowledgedAt).toMinutes();
        }
    }

    public void startInvestigation() {
        this.lifecycleStatus = "INVESTIGATING";
        this.investigatingAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void resolveIncident(String resolvedBy, String resolutionNotes) {
        this.lifecycleStatus = "RESOLVED";
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = resolvedBy;
        this.resolutionNotes = resolutionNotes;
        this.updatedAt = LocalDateTime.now();
        
        if (this.createdAt != null) {
            this.timeToResolveMinutes = (int) java.time.Duration.between(this.createdAt, this.resolvedAt).toMinutes();
        }
    }

    public void closeIncident() {
        this.lifecycleStatus = "CLOSED";
        this.closedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isSlaBreached() {
        return LocalDateTime.now().isAfter(this.slaDeadline);
    }

    public long getMinutesToSlaDeadline() {
        return java.time.Duration.between(LocalDateTime.now(), this.slaDeadline).toMinutes();
    }
}
