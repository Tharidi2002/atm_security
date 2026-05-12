package com.atm.alert.enums;

public enum AlertStatus {
    NEW("New"),
    ACKNOWLEDGED("Acknowledged"),
    INVESTIGATING("Investigating"),
    RESOLVED("Resolved"),
    FALSE_ALARM("False Alarm"),
    ESCALATED("Escalated");
    
    private final String description;
    
    AlertStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
