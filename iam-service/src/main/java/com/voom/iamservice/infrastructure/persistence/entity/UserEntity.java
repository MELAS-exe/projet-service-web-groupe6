package com.voom.iamservice.infrastructure.persistence.entity;

import com.voom.iamservice.domain.model.Role;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class UserEntity {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_entity_roles", // The name of the linking table
            joinColumns = @JoinColumn(name = "user_entity_id") // The foreign key column
    )
    @Column(name = "roles") // The column holding the actual role string
    private Set<Role> roles;
}
