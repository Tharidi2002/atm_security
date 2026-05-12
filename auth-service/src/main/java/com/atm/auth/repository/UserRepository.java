package com.atm.auth.repository;

import com.atm.auth.entity.User;
import com.atm.auth.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByBankId(Long bankId);
    
    List<User> findByRole(UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.accountLockedUntil > :now")
    List<User> findLockedUsers(@Param("now") LocalDateTime now);
    
    @Query("SELECT u FROM User u WHERE u.loginAttempts >= :maxAttempts AND u.accountLockedUntil IS NULL")
    List<User> findUsersWithMaxFailedAttempts(@Param("maxAttempts") int maxAttempts);
    
    @Query("SELECT u FROM User u WHERE u.passwordResetToken = :token AND u.passwordResetExpires > :now")
    Optional<User> findByPasswordResetToken(@Param("token") String token, @Param("now") LocalDateTime now);
    
    @Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token")
    Optional<User> findByEmailVerificationToken(@Param("token") String token);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.bankId = :bankId AND u.isActive = true")
    long countActiveUsersByBank(@Param("bankId") Long bankId);
    
    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.isEmailVerified = true")
    List<User> findActiveVerifiedUsers();
}
