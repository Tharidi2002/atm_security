package com.atm.auth.service;

import com.atm.auth.entity.User;
import com.atm.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AccountLockoutService {

    @Autowired
    private UserRepository userRepository;

    @Value("${atm.app.maxLoginAttempts:5}")
    private int maxLoginAttempts;

    @Value("${atm.app.lockoutDurationMinutes:15}")
    private int lockoutDurationMinutes;

    public void recordFailedLogin(String username) {
        User user = userRepository.findByUsername(username)
                .orElse(null);
        
        if (user != null) {
            int currentAttempts = user.getLoginAttempts() + 1;
            user.setLoginAttempts(currentAttempts);
            
            // Check if account should be locked
            if (currentAttempts >= maxLoginAttempts) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
            }
            
            userRepository.save(user);
        }
    }

    public void resetFailedAttempts(String username) {
        User user = userRepository.findByUsername(username)
                .orElse(null);
        
        if (user != null) {
            user.setLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }

    public boolean isAccountLocked(String username) {
        User user = userRepository.findByUsername(username)
                .orElse(null);
        
        if (user == null) {
            return false;
        }
        
        LocalDateTime lockedUntil = user.getLockedUntil();
        if (lockedUntil == null) {
            return false;
        }
        
        // Check if lockout period has expired
        if (lockedUntil.isBefore(LocalDateTime.now())) {
            // Auto-unlock the account
            user.setLockedUntil(null);
            user.setLoginAttempts(0);
            userRepository.save(user);
            return false;
        }
        
        return true;
    }

    public long getRemainingLockoutMinutes(String username) {
        User user = userRepository.findByUsername(username)
                .orElse(null);
        
        if (user == null || user.getLockedUntil() == null) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (user.getLockedUntil().isBefore(now)) {
            return 0;
        }
        
        return java.time.Duration.between(now, user.getLockedUntil()).toMinutes();
    }

    public void unlockAccount(String username) {
        User user = userRepository.findByUsername(username)
                .orElse(null);
        
        if (user != null) {
            user.setLockedUntil(null);
            user.setLoginAttempts(0);
            userRepository.save(user);
        }
    }
}
