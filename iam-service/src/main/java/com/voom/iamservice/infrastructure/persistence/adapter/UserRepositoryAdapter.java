package com.voom.iamservice.infrastructure.persistence.adapter;

import com.voom.iamservice.application.port.out.UserRepositoryPort;
import com.voom.iamservice.domain.model.User;
import com.voom.iamservice.infrastructure.mapper.UserMapper;
import com.voom.iamservice.infrastructure.persistence.entity.UserEntity;
import com.voom.iamservice.infrastructure.persistence.repository.SpringDataUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@AllArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {
    private final SpringDataUserRepository repository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return repository.findByPhoneNumber(phoneNumber).map(userMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity savedEntity = repository.save(userMapper.toEntity(user));
        return userMapper.toDomain(savedEntity);
    }
}
