CREATE TABLE IF NOT EXISTS accounts
(
    uuid    TEXT            NOT NULL,
    balance DECIMAL(65, 20) NOT NULL,
    world   TEXT            NULL,
    UNIQUE (uuid, world)
)