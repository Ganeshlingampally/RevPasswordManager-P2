-- ===================================================
-- RevPasswordManager P2 — Oracle SQL Schema
-- Tables: users, credentials, security_questions, verification_codes
-- ===================================================

-- ===== SEQUENCES =====

CREATE SEQUENCE users_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE credentials_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE security_questions_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE verification_codes_seq START WITH 1 INCREMENT BY 1;

-- ===== USERS TABLE =====

CREATE TABLE users (
    user_id        NUMBER(19)    DEFAULT users_seq.NEXTVAL PRIMARY KEY,
    username       VARCHAR2(50)  NOT NULL UNIQUE,
    email          VARCHAR2(100) NOT NULL UNIQUE,
    master_password_hash VARCHAR2(255) NOT NULL,
    first_name     VARCHAR2(50),
    last_name      VARCHAR2(50),
    phone_number   VARCHAR2(20),
    two_factor_enabled NUMBER(1) DEFAULT 0,
    created_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- ===== CREDENTIALS TABLE =====

CREATE TABLE credentials (
    credential_id     NUMBER(19)    DEFAULT credentials_seq.NEXTVAL PRIMARY KEY,
    user_id           NUMBER(19)    NOT NULL,
    site_name         VARCHAR2(100) NOT NULL,
    site_url          VARCHAR2(255),
    site_username     VARCHAR2(100) NOT NULL,
    encrypted_password VARCHAR2(500) NOT NULL,
    notes             VARCHAR2(500),
    category          VARCHAR2(50),
    favorite          NUMBER(1)     DEFAULT 0,
    created_at        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_credentials_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ===== SECURITY QUESTIONS TABLE =====

CREATE TABLE security_questions (
    question_id   NUMBER(19)    DEFAULT security_questions_seq.NEXTVAL PRIMARY KEY,
    user_id       NUMBER(19)    NOT NULL,
    question      VARCHAR2(255) NOT NULL,
    answer_hash   VARCHAR2(255) NOT NULL,
    created_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sq_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ===== VERIFICATION CODES TABLE =====

CREATE TABLE verification_codes (
    code_id    NUMBER(19)    DEFAULT verification_codes_seq.NEXTVAL PRIMARY KEY,
    user_id    NUMBER(19)    NOT NULL,
    code       VARCHAR2(10)  NOT NULL,
    purpose    VARCHAR2(50)  NOT NULL,
    expires_at TIMESTAMP     NOT NULL,
    used       NUMBER(1)     DEFAULT 0,
    created_at TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vc_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ===== INDEXES =====

CREATE INDEX idx_credentials_user_id ON credentials(user_id);
CREATE INDEX idx_credentials_site_name ON credentials(site_name);
CREATE INDEX idx_credentials_category ON credentials(category);
CREATE INDEX idx_sq_user_id ON security_questions(user_id);
CREATE INDEX idx_vc_user_id ON verification_codes(user_id);
CREATE INDEX idx_vc_code ON verification_codes(code);
