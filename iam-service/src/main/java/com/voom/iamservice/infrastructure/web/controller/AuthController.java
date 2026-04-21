package com.voom.iamservice.infrastructure.web.controller;

import com.voom.iamservice.application.port.in.LoginUseCase;
import com.voom.iamservice.application.port.in.RefreshTokenUseCase;
import com.voom.iamservice.application.port.in.RegisterUseCase;
import com.voom.iamservice.domain.model.TokenPair;
import com.voom.iamservice.domain.model.User;
import com.voom.iamservice.infrastructure.persistence.entity.UserEntity;
import com.voom.iamservice.infrastructure.web.dto.AuthResponse;
import com.voom.iamservice.infrastructure.web.dto.LoginRequest;
import com.voom.iamservice.infrastructure.web.dto.RefreshTokenRequest;
import com.voom.iamservice.infrastructure.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.antlr.v4.runtime.Token;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        TokenPair jwtToken = loginUseCase.login(loginRequest.phoneNumber(), loginRequest.password());
        return ResponseEntity.ok(new AuthResponse(jwtToken.accessToken(), jwtToken.refreshToken()));
    }

    @PostMapping("/register_consumer")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        User newUser = new User();
        newUser.setPhoneNumber(registerRequest.phoneNumber());
        newUser.setPassword(registerRequest.password());
        newUser.setFirstName(registerRequest.firstName());
        newUser.setLastName(registerRequest.lastName());

        TokenPair jwtToken = registerUseCase.registerConsumer(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(jwtToken.accessToken(), jwtToken.refreshToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest refreshRequest) {
        TokenPair tokens = refreshTokenUseCase.refreshToken(refreshRequest.refreshToken());

        return ResponseEntity.ok(new AuthResponse(tokens.accessToken(), tokens.refreshToken()));
    }
}
