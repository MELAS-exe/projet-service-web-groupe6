package com.voom.iamservice.infrastructure.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String username; // This will hold the phone number
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
}
