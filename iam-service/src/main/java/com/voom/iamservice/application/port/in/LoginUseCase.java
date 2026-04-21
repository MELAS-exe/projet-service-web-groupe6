package com.voom.iamservice.application.port.in;

import com.voom.iamservice.domain.model.TokenPair;

public interface LoginUseCase {
    TokenPair login(String phoneNumber, String rawPassword);
}
