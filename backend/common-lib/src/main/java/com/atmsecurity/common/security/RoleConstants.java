package com.atmsecurity.common.security;

import java.util.List;
import java.util.Map;

public final class RoleConstants {

    public static final String ADMIN = "ADMIN";
    public static final String BANK_MANAGER = "BANK_MANAGER";
    public static final String SECURITY_PERSONNEL = "SECURITY_PERSONNEL";

    private static final Map<String, List<String>> ROLE_PERMISSIONS = Map.of(
            ADMIN, List.of(
                    "ALERT_READ_ALL", "ALERT_ACK", "STATION_CRUD", "USER_CRUD",
                    "REPORT_GENERATE", "BANK_READ_ALL", "AUDIT_READ"
            ),
            BANK_MANAGER, List.of(
                    "ALERT_READ_BANK", "ALERT_ACK", "STATION_CRUD", "USER_READ_BANK",
                    "REPORT_GENERATE", "BANK_READ_OWN"
            ),
            SECURITY_PERSONNEL, List.of(
                    "ALERT_READ_BANK", "ALERT_ACK", "STATION_READ_BANK", "REPORT_READ"
            )
    );

    private RoleConstants() {
    }

    public static List<String> permissionsForRole(String roleName) {
        return ROLE_PERMISSIONS.getOrDefault(roleName, List.of());
    }
}
