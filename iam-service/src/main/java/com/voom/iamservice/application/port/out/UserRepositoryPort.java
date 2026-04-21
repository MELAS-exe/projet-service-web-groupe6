package com.voom.iamservice.application.port.out;

import com.voom.iamservice.domain.model.User;

import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findByPhoneNumber(String phoneNumber);
    User save(User user);
}
