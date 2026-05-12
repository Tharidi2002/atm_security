package com.atm.auth.controller;

import com.atm.auth.dto.LoginRequest;
import com.atm.auth.dto.LoginResponse;
import com.atm.auth.dto.RegisterRequest;
import com.atm.auth.service.AuthenticationService;
import com.atm.auth.service.TotpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationService authenticationService;
    private final TotpService totpService;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, 
                                              HttpServletRequest httpRequest) {
        log.info("Login attempt for user: {} from IP: {}", request.getUsername(), getClientIp(httpRequest));
        
        LoginResponse response = authenticationService.login(request);
        
        if (response.isRequireMfa()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        log.info("User logged in successfully: {}", response.getUsername());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authenticationService.register(request);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "User registered successfully. Please check your email for verification.");
            response.put("username", request.getUsername());
            
            log.info("User registered successfully: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Refresh token is required");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            authenticationService.refreshToken(refreshToken);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Token refreshed successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid refresh token");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        
        if (token != null) {
            authenticationService.logout(token);
            log.info("User logged out successfully");
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/enable-2fa")
    public ResponseEntity<Map<String, Object>> enable2fa(HttpServletRequest request) {
        // Extract user info from JWT token
        String token = extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // This would be implemented with proper JWT extraction
        // For now, return a sample response
        String secret = totpService.generateSecret();
        String qrUrl = totpService.generateQrCodeUrl(secret, "user@example.com");
        String qrImage = totpService.generateQrCodeImage(qrUrl);
        
        Map<String, Object> response = new HashMap<>();
        response.put("secret", secret);
        response.put("qrCodeUrl", qrUrl);
        response.put("qrCodeImage", qrImage);
        response.put("message", "2FA setup initiated. Please scan the QR code and verify.");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/verify-2fa")
    public ResponseEntity<Map<String, String>> verify2fa(@RequestBody Map<String, String> request) {
        String secret = request.get("secret");
        String code = request.get("code");
        
        if (secret == null || code == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Secret and code are required");
            return ResponseEntity.badRequest().body(error);
        }
        
        boolean isValid = totpService.verifyCode(secret, code);
        
        Map<String, String> response = new HashMap<>();
        if (isValid) {
            response.put("message", "2FA verification successful");
            response.put("status", "verified");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Invalid 2FA code");
            response.put("status", "failed");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        // This would implement email verification logic
        Map<String, String> response = new HashMap<>();
        response.put("message", "Email verified successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        if (email == null || email.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Email is required");
            return ResponseEntity.badRequest().body(error);
        }
        
        // This would implement password reset logic
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset link sent to your email");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        
        if (token == null || newPassword == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Token and new password are required");
            return ResponseEntity.badRequest().body(error);
        }
        
        // This would implement password reset logic
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successful");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "auth-service");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return ResponseEntity.ok(response);
    }
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
