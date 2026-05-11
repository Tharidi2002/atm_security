package com.atm.alert.entity;

public enum AlertCategory {
    FIRE_ALARM("Fire Alarm"),
    DOOR_OPEN("Door Open"),
    POWER_FAILURE("Power Failure"),
    PHYSICAL_TAMPERING("Physical Tampering"),
    CASH_THEFT("Cash Theft"),
    NETWORK_ISSUE("Network Issue"),
    OTHER("Other");

    private final String displayName;

    AlertCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
