package com.atmsecurity.auth.service;

import com.atmsecurity.auth.dto.AuthResponse;
import com.atmsecurity.auth.dto.LoginRequest;
import com.atmsecurity.auth.dto.RefreshTokenRequest;
import com.atmsecurity.auth.dto.RegisterRequest;
import com.atmsecurity.auth.dto.UserProfile;
import com.atmsecurity.auth.entity.AuditLog;
import com.atmsecurity.auth.entity.Bank;
import com.atmsecurity.auth.entity.RefreshToken;
import com.atmsecurity.auth.entity.Role;
import com.atmsecurity.auth.entity.User;
import com.atmsecurity.auth.exception.AuthException;
import com.atmsecurity.auth.repository.AuditLogRepository;
import com.atmsecurity.auth.repository.BankRepository;
import com.atmsecurity.auth.repository.RefreshTokenRepository;
import com.atmsecurity.auth.repository.RoleRepository;
import com.atmsecurity.auth.repository.UserRepository;
import com.atmsecurity.common.crypto.AesEncryptionService;
import com.atmsecurity.common.security.JwtProperties;
import com.atmsecurity.common.security.JwtTokenProvider;
import com.atmsecurity.common.security.RoleConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BankRepository bankRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final AesEncryptionService aesEncryptionService;

    @Value("${auth.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthException("Invalid username or password"));

        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            throw new AuthException("Account is locked due to multiple failed login attempts");
        }

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new AuthException("Account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new AuthException("Invalid username or password");
        }

        user.setFailedAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        logAudit(user, "LOGIN_SUCCESS", "User", String.valueOf(user.getId()), httpRequest, null);

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email already registered");
        }

        Bank bank = bankRepository.findById(request.getBankId())
                .orElseThrow(() -> new AuthException("Selected bank not found"));

        if (!Boolean.TRUE.equals(bank.getActive())) {
            throw new AuthException("Selected bank is not active");
        }

        Role role = roleRepository.findByName(RoleConstants.SECURITY_PERSONNEL)
                .orElseThrow(() -> new AuthException("Default role not configured"));

        User user = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .bank(bank)
                .role(role)
                .enabled(true)
                .accountLocked(false)
                .failedAttempts(0)
                .build();

        user = userRepository.save(user);

        Map<String, Object> details = new HashMap<>();
        details.put("bankId", bank.getId());
        details.put("bankName", bank.getName());
        logAudit(user, "USER_REGISTERED", "User", String.valueOf(user.getId()), httpRequest, details);

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String rawToken = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(rawToken)) {
            throw new AuthException("Invalid refresh token");
        }

        if (!"REFRESH".equals(jwtTokenProvider.getTokenType(rawToken))) {
            throw new AuthException("Invalid token type");
        }

        String tokenHash = aesEncryptionService.hashSha256(rawToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(() -> new AuthException("Refresh token not found or revoked"));

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new AuthException("Refresh token expired");
        }

        User user = stored.getUser();
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        String tokenHash = aesEncryptionService.hashSha256(refreshToken);
        refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    public UserProfile getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("User not found"));
        return toUserProfile(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String roleName = user.getRole().getName();
        Long bankId = user.getBank() != null ? user.getBank().getId() : null;
        List<String> permissions = RoleConstants.permissionsForRole(roleName);

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getUsername(), roleName, bankId, permissions);

        String refreshTokenRaw = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());
        String refreshTokenHash = aesEncryptionService.hashSha256(refreshTokenRaw);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(refreshTokenHash)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpirationMs() / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenRaw)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpirationMs() / 1000)
                .user(toUserProfile(user))
                .build();
    }

    private UserProfile toUserProfile(User user) {
        return UserProfile.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().getName())
                .bankId(user.getBank() != null ? user.getBank().getId() : null)
                .bankName(user.getBank() != null ? user.getBank().getName() : null)
                .permissions(RoleConstants.permissionsForRole(user.getRole().getName()))
                .build();
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);
        if (attempts >= maxFailedAttempts) {
            user.setAccountLocked(true);
        }
        userRepository.save(user);
    }

    private void logAudit(User user, String action, String entityType, String entityId,
                          HttpServletRequest request, Map<String, Object> details) {
        AuditLog log = AuditLog.builder()
                .user(user)
                .username(user.getUsername())
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .ipAddress(resolveClientIp(request))
                .userAgent(request != null ? request.getHeader("User-Agent") : null)
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
