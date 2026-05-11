package com.atm.auth.controller;

import com.atm.auth.dto.JwtResponse;
import com.atm.auth.dto.LoginRequest;
import com.atm.auth.dto.MessageResponse;
import com.atm.auth.dto.QRCodeResponse;
import com.atm.auth.dto.SignupRequest;
import com.atm.auth.dto.TwoFactorRequest;
import com.atm.auth.entity.Role;
import com.atm.auth.entity.User;
import com.atm.auth.repository.RoleRepository;
import com.atm.auth.repository.UserRepository;
import com.atm.auth.security.JwtUtils;
import com.atm.auth.security.UserPrinciple;
import com.atm.auth.service.AccountLockoutService;
import com.atm.auth.service.TwoFactorAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    AuthenticationManager authenticationManager;
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    RoleRepository roleRepository;
    
    @Autowired
    PasswordEncoder encoder;
    
    @Autowired
    JwtUtils jwtUtils;
    
    @Autowired
    AccountLockoutService accountLockoutService;
    
    @Autowired
    TwoFactorAuthService twoFactorAuthService;
    
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        
        String username = loginRequest.getUsername();
        
        // Check if account is locked
        if (accountLockoutService.isAccountLocked(username)) {
            long remainingMinutes = accountLockoutService.getRemainingLockoutMinutes(username);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Account is locked. Try again in " + remainingMinutes + " minutes."));
        }
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword()));
            
            // Reset failed attempts on successful login
            accountLockoutService.resetFailedAttempts(username);
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());
            
            // Update last login info
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                user.setLastLogin(LocalDateTime.now());
                user.setLastLoginIp(getClientIpAddress(request));
                userRepository.save(user);
            }
            
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    user != null ? user.getFirstName() : null,
                    user != null ? user.getLastName() : null,
                    roles));
                    
        } catch (Exception e) {
            // Record failed login attempt
            accountLockoutService.recordFailedLogin(username);
            throw e;
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }
        
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }
        
        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getFirstName(),
                signUpRequest.getLastName(),
                signUpRequest.getPhoneNumber());
        
        Set<String> strRoles = new HashSet<>();
        strRoles.add("ROLE_SECURITY_PERSONNEL"); // Default role
        
        Set<Role> roles = new HashSet<>();
        
        // Handle role from request if provided
        if (signUpRequest.getRole() != null && !signUpRequest.getRole().trim().isEmpty()) {
            strRoles.clear();
            strRoles.add(signUpRequest.getRole());
        }
        
        if (strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName("ROLE_SECURITY_PERSONNEL")
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                if ("ROLE_SUPER_ADMIN".equals(role)) {
                    Role adminRole = roleRepository.findByName("ROLE_SUPER_ADMIN")
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(adminRole);
                } else if ("ROLE_BANK_ADMIN".equals(role)) {
                    Role modRole = roleRepository.findByName("ROLE_BANK_ADMIN")
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(modRole);
                } else {
                    Role userRole = roleRepository.findByName("ROLE_SECURITY_PERSONNEL")
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(userRole);
                }
            });
        }
        
        user.setRoles(roles);
        userRepository.save(user);
        
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
    
    @PostMapping("/unlock-account")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> unlockAccount(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Username is required"));
        }
        
        accountLockoutService.unlockAccount(username.trim());
        return ResponseEntity.ok(new MessageResponse("Account unlocked successfully"));
    }
    
    @GetMapping("/account-status/{username}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BANK_ADMIN')")
    public ResponseEntity<?> getAccountStatus(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElse(null);
        
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> status = new HashMap<>();
        status.put("username", user.getUsername());
        status.put("isActive", user.getIsActive());
        status.put("loginAttempts", user.getLoginAttempts());
        status.put("isLocked", accountLockoutService.isAccountLocked(username));
        status.put("lockedUntil", user.getLockedUntil());
        status.put("lastLogin", user.getLastLogin());
        status.put("lastLoginIp", user.getLastLoginIp());
        
        if (accountLockoutService.isAccountLocked(username)) {
            status.put("remainingLockoutMinutes", accountLockoutService.getRemainingLockoutMinutes(username));
        }
        
        return ResponseEntity.ok(status);
    }
    
    @PostMapping("/enable-2fa")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> enableTwoFactor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        try {
            String secretKey = twoFactorAuthService.enableTwoFactor(username);
            String qrCode = twoFactorAuthService.generateQRCode(username, secretKey);
            
            QRCodeResponse response = new QRCodeResponse(qrCode, secretKey);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error generating QR code: " + e.getMessage()));
        }
    }
    
    @PostMapping("/confirm-2fa")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> confirmTwoFactor(@Valid @RequestBody TwoFactorRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        try {
            int code = Integer.parseInt(request.getVerificationCode());
            twoFactorAuthService.confirmTwoFactor(username, code);
            return ResponseEntity.ok(new MessageResponse("2FA enabled successfully"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid verification code format"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error enabling 2FA: " + e.getMessage()));
        }
    }
    
    @PostMapping("/disable-2fa")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> disableTwoFactor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        twoFactorAuthService.disableTwoFactor(username);
        return ResponseEntity.ok(new MessageResponse("2FA disabled successfully"));
    }
    
    @PostMapping("/verify-2fa")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> verifyTwoFactor(@Valid @RequestBody TwoFactorRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        try {
            int code = Integer.parseInt(request.getVerificationCode());
            boolean isValid = twoFactorAuthService.verifyTwoFactor(username, code);
            
            if (isValid) {
                return ResponseEntity.ok(new MessageResponse("2FA verification successful"));
            } else {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Invalid verification code"));
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid verification code format"));
        }
    }
    
    @GetMapping("/backup-codes-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getBackupCodesCount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        String count = twoFactorAuthService.getRemainingBackupCodes(username);
        return ResponseEntity.ok(Map.of("remainingCodes", count));
    }
}
