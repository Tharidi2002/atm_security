package com.atm.alert.enums;

public enum AlertCategory {
    UNAUTHORIZED_ACCESS("Unauthorized Access"),
    FIRE("Fire Detection"),
    TAMPERING("Tampering"),
    DOOR_OPEN("Door Open"),
    POWER_FAILURE("Power Failure"),
    ARMING_DISARMING("Arming/Disarming"),
    NETWORK_ISSUE("Network Issue"),
    LOW_BATTERY("Low Battery"),
    MAINTENANCE("Maintenance Required"),
    SYSTEM_ERROR("System Error"),
    UNKNOWN("Unknown");
    
    private final String description;
    
    AlertCategory(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
