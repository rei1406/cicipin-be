CREATE TYPE user_role AS ENUM (
    'CUSTOMER',
    'ADMIN',
    'DELIVERY_PARTNER',
    'RESTAURANT_OWNER'
);

CREATE TABLE users (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    username    VARCHAR(50)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        user_role    NOT NULL,
    is_verified BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active   BOOLEAN      NOT NULL DEFAULT FALSE,
    photo       VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email)
);
