package com.voom.iamservice.infrastructure.config;

import com.voom.iamservice.domain.event.UserRegisterEvent;
import com.voom.iamservice.application.port.out.PasswordEncoderPort;
import com.voom.iamservice.application.port.out.UserRepositoryPort;
import com.voom.iamservice.domain.model.Role;
import com.voom.iamservice.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder; // Using your port!
    private final ApplicationEventPublisher eventPublisher;

    @Value("${admin.default.phone:772222224}")
    private String defaultNumber;

    @Value("${admin.default.password:AdminDefault}")
    private String defaultPassword;

    @Override
    public void run(String... args) {
        // Strip spaces just to be safe
        String normalizedNumber = defaultNumber.replaceAll("\\s+", "");

        if(userRepository.findByPhoneNumber(normalizedNumber).isEmpty()) {
            User user = User.builder()
                    .id(UUID.randomUUID())
                    .firstName("Admin")
                    .lastName("Admin")
                    .phoneNumber(normalizedNumber)
                    .password(passwordEncoder.encode(defaultPassword))
                    .roles(Set.of(Role.SUPERADMIN))
                    .build();

            userRepository.save(user);
            eventPublisher.publishEvent(new UserRegisterEvent(user.getId(), user.getFirstName(), user.getLastName()));
            System.out.println("🛡️ Default Admin account seeded successfully!");
        }
    }
}
