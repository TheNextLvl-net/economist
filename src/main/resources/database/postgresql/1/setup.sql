CREATE TABLE IF NOT EXISTS accounts
(
    uuid
    TEXT
    NOT
    NULL,
    world
    TEXT
    NULL,
    currency
    TEXT
    NOT
    NULL,
    balance
    DECIMAL
(
    65,
    20
) NOT NULL
    );

CREATE UNIQUE INDEX IF NOT EXISTS accounts_uuid_world_currency_unique
    ON accounts (uuid, world, currency)
    WHERE world IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS accounts_uuid_global_currency_unique
    ON accounts (uuid, currency)
    WHERE world IS NULL;

CREATE INDEX IF NOT EXISTS accounts_world_currency_balance_idx
    ON accounts (world, currency, balance DESC);

CREATE TABLE IF NOT EXISTS banks
(
    name
    TEXT
    NOT
    NULL
    PRIMARY
    KEY,
    balance
    DECIMAL
(
    65,
    20
) NOT NULL,
    owner TEXT NOT NULL UNIQUE,
    members TEXT NOT NULL DEFAULT '[]'
    );

CREATE TABLE IF NOT EXISTS currencies
(
    name
    TEXT
    NOT
    NULL
    PRIMARY
    KEY,
    symbol
    TEXT
    NOT
    NULL,
    fractional_digits
    INTEGER
    NOT
    NULL,
    min_balance
    DECIMAL
(
    65,
    20
) NULL,
    max_balance DECIMAL
(
    65,
    20
) NULL
    );

CREATE TABLE IF NOT EXISTS currency_settings
(
    id INTEGER NOT NULL PRIMARY KEY,
    default_currency TEXT NOT NULL
    );

INSERT INTO currency_settings (id, default_currency)
VALUES (1, 'euro')
ON CONFLICT (id) DO NOTHING;

CREATE TABLE IF NOT EXISTS currency_translations
(
    currency_name
    TEXT
    NOT
    NULL
    REFERENCES
    currencies
(
    name
),
    locale TEXT NOT NULL,
    form TEXT NOT NULL,
    display_name TEXT NOT NULL,
    PRIMARY KEY
(
    currency_name,
    locale,
    form
)
    );
