package com.voom.iamservice.infrastructure.security;

import com.voom.iamservice.application.port.out.JwtPort;
import com.voom.iamservice.domain.model.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtPort jwtPort;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);

            // This implicitly validates the token's signature and expiration!
            // If it's forged or expired, it throws an exception and skips the rest.
            final String phoneNumber = jwtPort.extractPhoneNumber(jwt);
            final String userIdStr = jwtPort.extractUserId(jwt);
            final List<String> roleStrings = jwtPort.extractRoles(jwt);

            if(phoneNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 1. Convert role strings back to Spring Security Authorities
                List<SimpleGrantedAuthority> authorities = roleStrings.stream()
                        .map(role -> {
                            String roleName = role.toUpperCase(); // Normalize case just in case
                            if (!roleName.startsWith("ROLE_")) {
                                roleName = "ROLE_" + roleName;
                            }
                            return roleName;
                        })
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                // 2. Build the UserDetails directly from the token data! (NO DATABASE)
                CustomUserDetails userDetails = new CustomUserDetails(
                        UUID.fromString(userIdStr),
                        phoneNumber,
                        "", // Password is blank because they are already authenticated via JWT
                        authorities
                );

                // 3. Set the context
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(token);
            }
        } catch (Exception e) {
            // Token is invalid, expired, or malformed. Do nothing, let Spring block the request.
            System.out.println("Invalid JWT Token: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}