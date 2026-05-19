package com.cicipin.userservice.common.seed;

import com.cicipin.userservice.common.model.User;
import com.cicipin.userservice.common.model.UserRole;
import com.cicipin.userservice.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("seed")
@RequiredArgsConstructor
@Slf4j
public class SeedRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Starting seed...");

        seedAdminUser();

        log.info("Seed complete. Shutting down.");
        System.exit(0);
    }

    private void seedAdminUser() {
        if (userRepository.findByRole(UserRole.ADMIN).isPresent()) {
            log.info("Admin user already exists, skipping.");
            return;
        }

        User admin = User.builder()
                .username("admin")
                .name("Admin")
                .email("admin@cicipin.com")
                .password(passwordEncoder.encode("admin123"))
                .role(UserRole.ADMIN)
                .isVerified(true)
                .isActive(true)
                .build();

        userRepository.save(admin);
        log.info("Admin user created: admin / admin123");
    }
}
