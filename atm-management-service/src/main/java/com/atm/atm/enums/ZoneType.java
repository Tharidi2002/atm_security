package com.atm.atm.enums;

public enum ZoneType {
    CASH_COUNTER("Cash Counter"),
    PAWNING_AREA("Pawning Area"),
    GENERAL("General Zone"),
    HIGH_SECURITY("High Security Zone"),
    LOBBY("Lobby Area"),
    DRIVE_THROUGH("Drive Through");
    
    private final String description;
    
    ZoneType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
