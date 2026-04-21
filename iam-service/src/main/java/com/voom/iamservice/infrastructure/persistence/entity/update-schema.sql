CREATE TABLE user_entity_roles
(
    user_entity_id BINARY(16)   NOT NULL,
    roles          VARCHAR(255) NULL
);

CREATE TABLE users
(
    id           BINARY(16)   NOT NULL,
    first_name   VARCHAR(255) NULL,
    last_name    VARCHAR(255) NULL,
    phone_number VARCHAR(255) NULL,
    password     VARCHAR(255) NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE user_entity_roles
    ADD CONSTRAINT fk_userentity_roles_on_user_entity FOREIGN KEY (user_entity_id) REFERENCES users (id);
ALTER TABLE user_entity_roles
    DROP COLUMN roles;

ALTER TABLE user_entity_roles
    ADD roles SMALLINT NULL;