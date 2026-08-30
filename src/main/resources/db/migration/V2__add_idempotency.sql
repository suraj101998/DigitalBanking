-- V2__add_idempotency.sql
-- Idempotency key store for deduplication of payment/transfer requests (doc item #7)

CREATE TABLE IF NOT EXISTS idempotency_keys (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    idempotency_key  VARCHAR(64)  NOT NULL,
    transaction_id   VARCHAR(50)  NULL,
    response_status  INT          NULL,
    response_body    TEXT         NULL,
    created_at       DATETIME(6)  NOT NULL,
    customer_id      INT          NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_idempotency_key (idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add idempotency_key and version columns to transactions (for optimistic locking)
ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(64) NULL,
    ADD COLUMN IF NOT EXISTS version         BIGINT      NOT NULL DEFAULT 0,
    ADD UNIQUE KEY uk_transactions_idempotency_key (idempotency_key);
