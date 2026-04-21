package com.voom.iamservice.application.port.in;

import com.voom.iamservice.domain.model.TokenPair;
import com.voom.iamservice.domain.model.User;

public interface RegisterUseCase {
    TokenPair registerConsumer(User newUser);
    User registerSupplier(User newUser);
    User registerAdmin(User newUser);
}
