package com.bank.atm.controller;

import com.bank.atm.entity.User;
import com.bank.atm.entity.UserRole;
import com.bank.atm.repository.UserRepository;
import com.bank.atm.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173" })
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }

        if (!user.getIsActive()) {
            return ResponseEntity.status(403).body(Map.of("error", "Account is disabled"));
        }

        // Update last login
        user.updateLastLogin();
        userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().toString(),
                user.getId(),
                user.getBank() != null ? user.getBank().getId() : null);

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("fullName", user.getFullName());
        userMap.put("email", user.getEmail());
        userMap.put("role", user.getRole().toString());

        if (user.getBank() != null) {
            userMap.put("bankId", user.getBank().getId());
            userMap.put("bankName", user.getBank().getName());
        } else {
            userMap.put("bankId", null);
            userMap.put("bankName", null);
        }

        response.put("token", token);
        response.put("user", userMap);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registerRequest) {
        String username = registerRequest.get("username");
        String password = registerRequest.get("password");
        String email = registerRequest.get("email");
        String fullName = registerRequest.get("fullName");
        String roleStr = registerRequest.get("role");

        // Validate
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
        }
        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters"));
        }

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }

        if (email != null && !email.isEmpty() && userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }

        // Create user
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .fullName(fullName != null ? fullName : username)
                .role(roleStr != null ? UserRole.valueOf(roleStr.toUpperCase()) : UserRole.BANK_USER)
                .isActive(true)
                .build();

        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("username", user.getUsername());
        response.put("role", user.getRole().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        User user = userOpt.get();

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("fullName", user.getFullName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole().toString());

        if (user.getBank() != null) {
            response.put("bankId", user.getBank().getId());
            response.put("bankName", user.getBank().getName());
        } else {
            response.put("bankId", null);
            response.put("bankName", null);
        }

        return ResponseEntity.ok(response);
    }
}