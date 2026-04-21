package com.voom.iamservice.application.port.in;

import com.voom.iamservice.domain.model.TokenPair;

public interface RefreshTokenUseCase {
    TokenPair refreshToken(String refreshToken);
}
