package com.voom.iamservice.infrastructure.mapper;

import com.voom.iamservice.domain.model.User;
import com.voom.iamservice.infrastructure.persistence.entity.UserEntity;
import com.voom.iamservice.infrastructure.web.dto.AdminAuthResponse;
import com.voom.iamservice.infrastructure.web.dto.AuthResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserEntity toEntity(User domain);
    User toDomain(UserEntity entity);
    AdminAuthResponse toAuthResponse(User domain);
}
