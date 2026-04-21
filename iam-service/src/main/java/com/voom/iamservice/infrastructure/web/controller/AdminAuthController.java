package com.voom.iamservice.infrastructure.web.controller;

import com.voom.iamservice.application.port.in.RegisterUseCase;
import com.voom.iamservice.domain.model.User;
import com.voom.iamservice.infrastructure.mapper.UserMapper;
import com.voom.iamservice.infrastructure.web.dto.AdminAuthResponse;
import com.voom.iamservice.infrastructure.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/auth")
public class AdminAuthController {
    private final RegisterUseCase registerUseCase;
    private final UserMapper userMapper;

    @PreAuthorize("hasRole('SUPERADMIN')")
    @PostMapping("/register_admin")
    public ResponseEntity<AdminAuthResponse> registerAdmin(@Valid @RequestBody RegisterRequest registerRequest) {
        User newUser = User.builder()
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .password(registerRequest.password())
                .phoneNumber(registerRequest.phoneNumber())
                .build();
        AdminAuthResponse response = userMapper.toAuthResponse(registerUseCase.registerAdmin(newUser));
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @PostMapping("/register_warehouse_admin")
    public ResponseEntity<AdminAuthResponse> registerWarehouseAdmin(@Valid @RequestBody RegisterRequest registerRequest) {
        User newUser = User.builder()
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .password(registerRequest.password())
                .phoneNumber(registerRequest.phoneNumber())
                .build();
        AdminAuthResponse response = userMapper.toAuthResponse(registerUseCase.registerSupplier(newUser));
        return ResponseEntity.ok(response);
    }
}
