package com.atm.auth.service;

import com.atm.auth.dto.LoginRequest;
import com.atm.auth.dto.LoginResponse;
import com.atm.auth.dto.RegisterRequest;
import com.atm.auth.entity.User;
import com.atm.auth.enums.UserRole;
import com.atm.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TotpService totpService;
    private final EmailService emailService;
    
    private final int maxLoginAttempts = 5;
    private final long lockoutDuration = 900000; // 15 minutes
    
    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            // Check if user exists and is not locked
            User user = userRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            if (user.isAccountLocked()) {
                throw new LockedException("Account is locked. Please try again later.");
            }
            
            if (!user.getIsActive()) {
                throw new LockedException("Account is disabled.");
            }
            
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            
            // Reset login attempts on successful login
            if (user.getLoginAttempts() > 0) {
                user.setLoginAttempts(0);
                user.setAccountLockedUntil(null);
                userRepository.save(user);
            }
            
            // Check 2FA if enabled
            if (user.isMfaEnabled()) {
                if (request.getTotpCode() == null || request.getTotpCode().isEmpty()) {
                    return LoginResponse.builder()
                            .requireMfa(true)
                            .message("2FA code required")
                            .build();
                }
                
                if (!totpService.verifyCode(user.getMfaSecret(), request.getTotpCode())) {
                    throw new BadCredentialsException("Invalid 2FA code");
                }
            }
            
            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            
            // Generate tokens
            String accessToken = jwtService.generateToken(
                user.getUsername(), 
                user.getId(), 
                user.getRole().name(), 
                user.getBankId()
            );
            
            String refreshToken = jwtService.generateRefreshToken(user.getUsername());
            
            log.info("User logged in successfully: {}", user.getUsername());
            
            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .role(user.getRole().name())
                    .bankId(user.getBankId())
                    .language(user.getLanguage().name())
                    .requireMfa(false)
                    .build();
                    
        } catch (BadCredentialsException e) {
            handleFailedLogin(request.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }
    }
    
    @Transactional
    public void register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : UserRole.SECURITY_OFFICER)
                .bankId(request.getBankId())
                .language(request.getLanguage())
                .isActive(false) // Require email verification
                .emailVerificationToken(UUID.randomUUID().toString())
                .build();
        
        userRepository.save(user);
        
        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), user.getEmailVerificationToken());
        
        log.info("User registered successfully: {}", user.getUsername());
    }
    
    @Transactional
    public void refreshToken(String refreshToken) {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        
        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // Generate new access token
        String newAccessToken = jwtService.generateToken(
            user.getUsername(), 
            user.getId(), 
            user.getRole().name(), 
            user.getBankId()
        );
        
        // Invalidate old refresh token
        jwtService.invalidateRefreshToken(refreshToken);
        
        // Generate new refresh token
        String newRefreshToken = jwtService.generateRefreshToken(user.getUsername());
        
        log.info("Token refreshed for user: {}", username);
    }
    
    @Transactional
    public void logout(String token) {
        jwtService.invalidateToken(token);
    }
    
    private void handleFailedLogin(String username) {
        try {
            User user = userRepository.findByUsernameOrEmail(username, username).orElse(null);
            if (user != null) {
                user.setLoginAttempts(user.getLoginAttempts() + 1);
                
                if (user.getLoginAttempts() >= maxLoginAttempts) {
                    user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(15));
                    log.warn("Account locked due to too many failed attempts: {}", username);
                }
                
                userRepository.save(user);
            }
        } catch (Exception e) {
            log.error("Error handling failed login for user: {}", username, e);
        }
    }
    
    @Transactional
    public void enable2fa(Long userId, String secret) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        user.setMfaSecret(secret);
        userRepository.save(user);
        
        log.info("2FA enabled for user: {}", user.getUsername());
    }
    
    @Transactional
    public void disable2fa(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        user.setMfaSecret(null);
        userRepository.save(user);
        
        log.info("2FA disabled for user: {}", user.getUsername());
    }
}
