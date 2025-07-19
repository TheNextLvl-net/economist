CREATE TABLE IF NOT EXISTS banks
(
    name    TEXT            NOT NULL UNIQUE PRIMARY KEY,
    balance DECIMAL(65, 20) NOT NULL,
    owner   TEXT            NOT NULL UNIQUE,
    members LIST            NOT NULL
)