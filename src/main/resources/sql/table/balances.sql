CREATE TABLE IF NOT EXISTS balances
(
    id       INTEGER         NOT NULL PRIMARY KEY REFERENCES accounts (id),
    balance  DECIMAL(65, 20) NOT NULL,
    currency TEXT            NOT NULL FOREIGN KEY REFERENCES currencies (name),
    UNIQUE (id, currency)
);