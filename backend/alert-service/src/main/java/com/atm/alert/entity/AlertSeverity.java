package com.atm.alert.entity;

public enum AlertSeverity {
    CRITICAL("Critical"),
    WARNING("Warning"),
    INFO("Info");

    private final String displayName;

    AlertSeverity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
