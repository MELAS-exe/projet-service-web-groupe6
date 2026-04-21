package com.voom.iamservice.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterEvent {
    private UUID userId;
    private String firstName;
    private String lastName;
}