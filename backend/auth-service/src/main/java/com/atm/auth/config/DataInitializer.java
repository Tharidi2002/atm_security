package com.atm.auth.config;

import com.atm.auth.entity.Role;
import com.atm.auth.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
    }
    
    private void initializeRoles() {
        if (!roleRepository.existsByName("ROLE_SUPER_ADMIN")) {
            Role superAdminRole = new Role();
            superAdminRole.setName("ROLE_SUPER_ADMIN");
            superAdminRole.setDescription("Super Administrator with full system access");
            roleRepository.save(superAdminRole);
        }
        
        if (!roleRepository.existsByName("ROLE_BANK_ADMIN")) {
            Role bankAdminRole = new Role();
            bankAdminRole.setName("ROLE_BANK_ADMIN");
            bankAdminRole.setDescription("Bank Administrator with bank-level access");
            roleRepository.save(bankAdminRole);
        }
        
        if (!roleRepository.existsByName("ROLE_SECURITY_PERSONNEL")) {
            Role securityRole = new Role();
            securityRole.setName("ROLE_SECURITY_PERSONNEL");
            securityRole.setDescription("Security Personnel with monitoring access");
            roleRepository.save(securityRole);
        }
    }
}
