package com.atmsecurity.auth.config;

import com.atmsecurity.auth.entity.Bank;
import com.atmsecurity.auth.entity.Role;
import com.atmsecurity.auth.entity.User;
import com.atmsecurity.auth.repository.BankRepository;
import com.atmsecurity.auth.repository.RoleRepository;
import com.atmsecurity.auth.repository.UserRepository;
import com.atmsecurity.common.security.RoleConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final BankRepository bankRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedRoles();
        seedBanks();
        seedAdminUser();
    }

    private void seedRoles() {
        List.of(
                new String[]{RoleConstants.ADMIN, "System administrator with full access"},
                new String[]{RoleConstants.BANK_MANAGER, "Bank manager with bank-scoped management access"},
                new String[]{RoleConstants.SECURITY_PERSONNEL, "Security officer with bank-scoped read/ack access"}
        ).forEach(data -> roleRepository.findByName(data[0]).orElseGet(() ->
                roleRepository.save(Role.builder().name(data[0]).description(data[1]).build())));
    }

    private void seedBanks() {
        List.of(
                new String[]{"Commercial Bank of Ceylon", "CBSL"},
                new String[]{"People's Bank", "PB"},
                new String[]{"Bank of Ceylon", "BOC"},
                new String[]{"Hatton National Bank", "HNB"},
                new String[]{"Sampath Bank", "SAMPATH"}
        ).forEach(data -> bankRepository.findByCode(data[1]).orElseGet(() ->
                bankRepository.save(Bank.builder().name(data[0]).code(data[1]).active(true).build())));
    }

    private void seedAdminUser() {
        if (userRepository.findByUsername("admin").isPresent()) {
            return;
        }
        Role adminRole = roleRepository.findByName(RoleConstants.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role missing"));

        User admin = User.builder()
                .username("admin")
                .email("admin@atmsecurity.lk")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .fullName("System Administrator")
                .role(adminRole)
                .enabled(true)
                .accountLocked(false)
                .failedAttempts(0)
                .build();

        userRepository.save(admin);
        log.info("Default admin user created: admin / Admin@123");
    }
}
