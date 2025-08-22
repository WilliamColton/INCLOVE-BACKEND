CREATE TABLE user_auth
(
    id                BIGINT AUTO_INCREMENT    NOT NULL,
    email             VARCHAR(255)             NOT NULL,
    user_id           VARCHAR(64)              NOT NULL,
    encoded_password  VARCHAR(255)             NOT NULL,
    verified          TINYINT(1) DEFAULT 0     NULL,
    verification_code VARCHAR(16)              NULL,
    created_at        datetime   DEFAULT NOW() NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE user_interest
(
    id         BIGINT AUTO_INCREMENT  NOT NULL,
    user_id    VARCHAR(64)            NOT NULL,
    interest   VARCHAR(128)           NOT NULL,
    created_at datetime DEFAULT NOW() NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE user_preference
(
    id                BIGINT AUTO_INCREMENT  NOT NULL,
    user_id           VARCHAR(64)            NOT NULL,
    preferred_age_min INT                    NULL,
    preferred_age_max INT                    NULL,
    accepted_genders  JSON                   NULL,
    preferred_traits  JSON                   NULL,
    preferred_hobbies JSON                   NULL,
    updated_at        datetime DEFAULT NOW() NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE user_profile
(
    id             BIGINT AUTO_INCREMENT  NOT NULL,
    user_id        VARCHAR(64)            NOT NULL,
    user_name      VARCHAR(128)           NULL,
    gender         VARCHAR(16)            NOT NULL,
    birthday       date                   NULL,
    intro          LONGTEXT               NULL,
    last_active_at datetime               NULL,
    update_at      datetime DEFAULT NOW() NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE user_trait
(
    id         BIGINT AUTO_INCREMENT  NOT NULL,
    user_id    VARCHAR(64)            NOT NULL,
    trait      VARCHAR(128)           NOT NULL,
    created_at datetime DEFAULT NOW() NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

ALTER TABLE user_auth
    ADD CONSTRAINT email UNIQUE (email);

ALTER TABLE user_interest
    ADD CONSTRAINT uq_user_interest UNIQUE (user_id, interest);

ALTER TABLE user_auth
    ADD CONSTRAINT user_id UNIQUE (user_id);

ALTER TABLE user_trait
    ADD CONSTRAINT uq_user_trait UNIQUE (user_id, trait);

ALTER TABLE user_preference
    ADD CONSTRAINT user_id UNIQUE (user_id);

ALTER TABLE user_profile
    ADD CONSTRAINT user_id UNIQUE (user_id);

CREATE INDEX idx_interest_user ON user_interest (user_id);

CREATE INDEX idx_trait_user ON user_trait (user_id);