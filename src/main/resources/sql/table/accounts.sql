CREATE TABLE IF NOT EXISTS accounts
(
    id          INTEGER   NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
    uuid        TEXT      NOT NULL,
    world       TEXT      NULL,
    last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (uuid, world)
);