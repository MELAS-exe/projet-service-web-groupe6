package com.voom.iamservice.application.port.out;

import com.voom.iamservice.domain.model.Role;
import com.voom.iamservice.domain.model.User;

import java.util.List;

public interface JwtPort {
    String generateToken(User user);
    String generateRefreshToken(User user);
    String extractPhoneNumber(String token);
    String extractUserId(String token);
    List<String> extractRoles(String token);
    boolean isTokenValid(String token, User user);
}
