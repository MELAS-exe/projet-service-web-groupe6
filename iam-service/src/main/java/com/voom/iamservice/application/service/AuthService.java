package com.voom.iamservice.application.service;

import com.voom.iamservice.domain.event.UserRegisterEvent;
import com.voom.iamservice.application.port.in.LoginUseCase;
import com.voom.iamservice.application.port.in.RefreshTokenUseCase;
import com.voom.iamservice.application.port.in.RegisterUseCase;
import com.voom.iamservice.application.port.out.JwtPort;
import com.voom.iamservice.application.port.out.PasswordEncoderPort;
import com.voom.iamservice.application.port.out.UserRepositoryPort;
import com.voom.iamservice.domain.exception.InvalidCredentialsException;
import com.voom.iamservice.domain.exception.UserAlreadyExistsException;
import com.voom.iamservice.domain.exception.UserNotFoundException;
import com.voom.iamservice.domain.model.Role;
import com.voom.iamservice.domain.model.TokenPair;
import com.voom.iamservice.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService implements LoginUseCase, RegisterUseCase, RefreshTokenUseCase {

    private final ApplicationEventPublisher eventPublisher;
    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final JwtPort jwtPort;

    private String sanitizePhoneNumber(String rawPhoneNumber) {
        if (rawPhoneNumber == null) return null;
        return rawPhoneNumber.replaceAll("[\\s\\-]", ""); // Strips spaces and dashes
    }

    @Override
    public TokenPair login(String phoneNumber, String rawPassword) {
        String cleanPhone = sanitizePhoneNumber(phoneNumber);

        User user = userRepository.findByPhoneNumber(cleanPhone).orElseThrow(
                () -> new UserNotFoundException("User not found")
        );

        if(!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Wrong password");
        }

        String accessToken = jwtPort.generateToken(user); // or newUser
        String refreshToken = jwtPort.generateRefreshToken(user);

        return new TokenPair(accessToken, refreshToken);
    }

    @Override
    public TokenPair registerConsumer(User newUser) {
        String cleanPhone = sanitizePhoneNumber(newUser.getPhoneNumber());
        newUser.setPhoneNumber(cleanPhone);

        Optional<User> user = userRepository.findByPhoneNumber(cleanPhone);
        if (user.isPresent()) {
            throw new UserAlreadyExistsException("This user with this phone number already exists");
        }

        String encodedPassword = passwordEncoder.encode(newUser.getPassword());

        newUser.setPassword(encodedPassword);
        newUser.setId(UUID.randomUUID());
        newUser.setRoles(Set.of(Role.CONSUMER, Role.PRODUCER));

        userRepository.save(newUser);
        eventPublisher.publishEvent(new UserRegisterEvent(newUser.getId(), newUser.getFirstName(), newUser.getLastName()));

        String accessToken = jwtPort.generateToken(newUser); // or newUser
        String refreshToken = jwtPort.generateRefreshToken(newUser);

        return new TokenPair(accessToken, refreshToken);
    }

    @Override
    public User registerSupplier(User newUser) {

        Optional<User> user = userRepository.findByPhoneNumber(newUser.getPhoneNumber());
        if (user.isPresent()) {
            throw new UserAlreadyExistsException("This user with this phone number already exists");
        }
        String encodedPassword = passwordEncoder.encode(newUser.getPassword());

        newUser.setPassword(encodedPassword);
        newUser.setId(UUID.randomUUID());
        newUser.setRoles(Set.of(Role.WAREHOUSEADMIN));

        User res = userRepository.save(newUser);
        eventPublisher.publishEvent(new UserRegisterEvent(newUser.getId(), newUser.getFirstName(), newUser.getLastName()));
        return res;
    }

    @Override
    public User registerAdmin(User newUser) {
        Optional<User> user = userRepository.findByPhoneNumber(newUser.getPhoneNumber());
        if (user.isPresent()) {
            throw new UserAlreadyExistsException("This user with this phone number already exists");
        }

        String encodedPassword = passwordEncoder.encode(newUser.getPassword());

        newUser.setPassword(encodedPassword);
        newUser.setId(UUID.randomUUID());
        newUser.setRoles(Set.of(Role.ADMIN));

        User res = userRepository.save(newUser);
        eventPublisher.publishEvent(new UserRegisterEvent(newUser.getId(), newUser.getFirstName(), newUser.getLastName()));
        return res;
    }

    @Override
    public TokenPair refreshToken(String refreshToken) {
        // 1. Extract the phone number from the token (JJWT will throw an exception here if the token is completely malformed or expired)
        String phoneNumber = jwtPort.extractPhoneNumber(refreshToken);

        if (phoneNumber == null) {
            throw new InvalidCredentialsException("Invalid refresh token format.");
        }

        // 2. Fetch the user from the DB
        User user = userRepository.findByPhoneNumber(phoneNumber).orElseThrow(
                () -> new UserNotFoundException("User not found.")
        );

        // 3. Validate the token against the user
        if (!jwtPort.isTokenValid(refreshToken, user)) {
            throw new InvalidCredentialsException("Refresh token is expired or invalid.");
        }

        // 4. Generate a fresh TokenPair (This is called "Refresh Token Rotation")
        String newAccessToken = jwtPort.generateToken(user);
        String newRefreshToken = jwtPort.generateRefreshToken(user);

        return new TokenPair(newAccessToken, newRefreshToken);
    }
}
