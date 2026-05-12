package com.atm.auth.enums;

public enum Language {
    EN("English"),
    SI("Sinhala"),
    TA("Tamil");
    
    private final String description;
    
    Language(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
