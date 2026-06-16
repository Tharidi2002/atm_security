package com.atmsecurity.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 80)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100)
    private String password;
}
