package com.hub.api.admin.config;

import com.hub.api.admin.entity.Role;
import com.hub.api.admin.entity.User;
import com.hub.api.admin.repository.RoleRepository;
import com.hub.api.admin.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class AppConfig {

    @Bean
    CommandLineRunner initAdmin(RoleRepository roleRepository,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        return args -> {
            Role roleUser = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

            Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("1234qwer"));
                admin.setRoles(Set.of(roleAdmin, roleUser));
                userRepository.save(admin);
            }
        };
    }
}
