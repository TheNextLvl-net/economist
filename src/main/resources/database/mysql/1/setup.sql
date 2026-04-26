CREATE TABLE IF NOT EXISTS accounts
(
    uuid
    VARCHAR
(
    36
) NOT NULL,
    world VARCHAR
(
    255
) NULL,
    currency VARCHAR
(
    255
) NOT NULL,
    balance DECIMAL
(
    65,
    20
) NOT NULL,
    world_scope VARCHAR
(
    255
) GENERATED ALWAYS AS
(
    COALESCE
(
    world,
    '__global__'
)) STORED,
    UNIQUE KEY accounts_uuid_scope_currency_unique
(
    uuid,
    world_scope,
    currency
),
    KEY accounts_world_currency_balance_idx
(
    world,
    currency,
    balance
)
    );

CREATE TABLE IF NOT EXISTS banks
(
    name
    VARCHAR
(
    255
) NOT NULL PRIMARY KEY,
    balance DECIMAL
(
    65,
    20
) NOT NULL,
    owner VARCHAR
(
    36
) NOT NULL UNIQUE,
    members LONGTEXT NOT NULL DEFAULT '[]'
    );

CREATE TABLE IF NOT EXISTS currencies
(
    name
    VARCHAR
(
    255
) NOT NULL PRIMARY KEY,
    symbol LONGTEXT NOT NULL,
    fractional_digits INT NOT NULL,
    min_balance DECIMAL
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
    id INT NOT NULL PRIMARY KEY,
    default_currency VARCHAR
(
    255
) NOT NULL
    );

INSERT IGNORE INTO currency_settings (id, default_currency)
VALUES (1, 'euro');

CREATE TABLE IF NOT EXISTS currency_translations
(
    currency_name
    VARCHAR
(
    255
) NOT NULL,
    locale VARCHAR
(
    64
) NOT NULL,
    form VARCHAR
(
    16
) NOT NULL,
    display_name LONGTEXT NOT NULL,
    PRIMARY KEY
(
    currency_name,
    locale,
    form
),
    CONSTRAINT fk_currency_translations_currency
    FOREIGN KEY
(
    currency_name
) REFERENCES currencies
(
    name
)
    );
