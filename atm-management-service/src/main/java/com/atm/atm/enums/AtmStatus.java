package com.atm.atm.enums;

public enum AtmStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    MAINTENANCE("Under Maintenance"),
    OFFLINE("Offline"),
    ERROR("Error");
    
    private final String description;
    
    AtmStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
