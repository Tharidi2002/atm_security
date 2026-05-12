package com.atm.auth.enums;

public enum UserRole {
    SUPER_ADMIN("Super Administrator"),
    BANK_ADMIN("Bank Administrator"),
    SECURITY_OFFICER("Security Officer");
    
    private final String description;
    
    UserRole(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
