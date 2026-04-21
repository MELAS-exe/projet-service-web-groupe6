package com.voom.iamservice.infrastructure.security;

import com.voom.iamservice.infrastructure.persistence.entity.UserEntity;
import com.voom.iamservice.infrastructure.persistence.repository.SpringDataUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SpringDataUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByPhoneNumber(username).orElseThrow(
                () -> new UsernameNotFoundException("User with username: " + username + " not found")
        );

        // Map roles to SimpleGrantedAuthority
        List<SimpleGrantedAuthority> authorities = userEntity.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .toList();

        // Return YOUR custom object containing the UUID
        return new CustomUserDetails(
                userEntity.getId(),
                userEntity.getPhoneNumber(),
                userEntity.getPassword(),
                authorities
        );
    }
}
