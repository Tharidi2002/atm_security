package com.atmsecurity.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private Long bankId;
    private String bankName;
    private List<String> permissions;
}
