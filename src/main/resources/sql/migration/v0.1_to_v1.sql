BEGIN TRANSACTION;

ALTER TABLE accounts
    RENAME TO accounts_old;

CREATE TABLE IF NOT EXISTS accounts
(
    id          INTEGER   NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
    uuid        TEXT      NOT NULL,
    world       TEXT      NULL,
    last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (uuid, world)
);

INSERT INTO accounts (uuid, world)
SELECT uuid, world
FROM accounts_old;

INSERT INTO balances (id, balance, currency)
SELECT id,
       (SELECT balance
        FROM accounts_old
        WHERE accounts_old.uuid = accounts.uuid
          AND accounts_old.world IS NOT DISTINCT FROM accounts.world
        LIMIT 1),
       ?
FROM accounts;

DROP TABLE accounts_old;

DROP TABLE IF EXISTS banks; -- v0.1 did not have bank support but created the table anyway

DROP TRIGGER IF EXISTS enforce_unique_uuid_world;
DROP TRIGGER IF EXISTS enforce_unique_uuid_world_update;

COMMIT;