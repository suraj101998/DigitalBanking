-- V1__initial_schema.sql
-- Initial schema for DigitalBanking (doc item #15 — Flyway migration)
-- Enabled when spring.flyway.enabled=true is set (after schema stabilisation)

-- ── customers ────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS customers (
    customer_id    INT          NOT NULL AUTO_INCREMENT,
    customer_name  VARCHAR(100) NOT NULL,
    account_number VARCHAR(20)  NULL,
    identity_type  VARCHAR(30)  NULL,
    identity_number VARCHAR(50) NULL,
    date_of_birth  DATE         NULL,
    mobile_number  VARCHAR(15)  NULL,
    email          VARCHAR(150) NULL,
    address        VARCHAR(300) NULL,
    sex            VARCHAR(10)  NULL,
    user_id        INT          NULL,
    account_status VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (customer_id),
    UNIQUE KEY uk_customers_email           (email),
    UNIQUE KEY uk_customers_identity_number (identity_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── users ────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id                     INT          NOT NULL AUTO_INCREMENT,
    user_name              VARCHAR(100) NOT NULL,
    password               VARCHAR(255) NOT NULL,
    active                 TINYINT(1)   NOT NULL DEFAULT 1,
    roles                  VARCHAR(255) NOT NULL,
    customer_id            INT          NULL,
    failed_login_attempts  INT          NOT NULL DEFAULT 0,     -- doc item #65
    locked_until           DATETIME(6)  NULL,                   -- doc item #65
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_user_name (user_name),
    CONSTRAINT fk_users_customer FOREIGN KEY (customer_id) REFERENCES customers (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── bank_accounts ─────────────────────────────────────────────────────────────
-- Separate account/balance table (doc items #2, #3)
CREATE TABLE IF NOT EXISTS bank_accounts (
    account_id      BIGINT          NOT NULL AUTO_INCREMENT,
    account_number  VARCHAR(20)     NOT NULL,
    balance         DECIMAL(19, 4)  NOT NULL DEFAULT 0.0000,
    initial_deposit DECIMAL(19, 4)  NOT NULL DEFAULT 0.0000,
    currency        VARCHAR(3)      NOT NULL DEFAULT 'INR',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    version         BIGINT          NOT NULL DEFAULT 0,     -- optimistic locking (doc item #61)
    customer_id     INT             NOT NULL,
    PRIMARY KEY (account_id),
    UNIQUE KEY uk_bank_accounts_account_number (account_number),
    CONSTRAINT fk_bank_accounts_customer FOREIGN KEY (customer_id) REFERENCES customers (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── transactions ─────────────────────────────────────────────────────────────
-- BigDecimal amounts, Instant timestamp, enum columns, TransactionStatus (doc items #4, #5, #19, #21)
CREATE TABLE IF NOT EXISTS transactions (
    serial_number        INT             NOT NULL AUTO_INCREMENT,
    transaction_id       VARCHAR(50)     NOT NULL,
    transaction_type     VARCHAR(10)     NOT NULL,          -- CREDIT / DEBIT
    transaction_mode     VARCHAR(20)     NULL,              -- UPI / NEFT / RTGS / IMPS / CASH
    transaction_status   VARCHAR(20)     NOT NULL DEFAULT 'SUCCESS',
    transaction_to       VARCHAR(100)    NULL,
    transaction_date     DATETIME(6)     NOT NULL,          -- Instant precision (doc item #19)
    transaction_amount   DECIMAL(19, 4)  NOT NULL,
    initial_deposit      DECIMAL(19, 4)  NOT NULL,
    available_balance    DECIMAL(19, 4)  NOT NULL,
    customer_id          INT             NOT NULL,
    PRIMARY KEY (serial_number),
    UNIQUE KEY uk_transactions_transaction_id (transaction_id),     -- doc item #13
    INDEX idx_transactions_customer_date (customer_id, transaction_date DESC), -- doc item #18
    CONSTRAINT fk_transactions_customer FOREIGN KEY (customer_id)  -- doc item #14
        REFERENCES customers (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── refresh_tokens ────────────────────────────────────────────────────────────
-- Persistent refresh tokens for JWT rotation (doc items #67, #68)
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    token       VARCHAR(128) NOT NULL,
    user_id     INT          NOT NULL,
    expires_at  DATETIME(6)  NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_token   (token),
    UNIQUE KEY uk_refresh_tokens_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
