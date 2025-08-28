CREATE TABLE IF NOT EXISTS balances
(
    id       INTEGER         NOT NULL,
    balance  DECIMAL(65, 20) NOT NULL,
    currency TEXT            NOT NULL,
    FOREIGN KEY (id) REFERENCES accounts (id),
    FOREIGN KEY (currency) REFERENCES currencies (name),
    UNIQUE (id, currency)
);