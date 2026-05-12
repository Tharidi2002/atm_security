package com.atm.alert.enums;

public enum AlertSeverity {
    CRITICAL("Critical"),
    WARNING("Warning"),
    INFO("Informational");
    
    private final String description;
    
    AlertSeverity(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
