CREATE TABLE IF NOT EXISTS banks
(
    id          INTEGER   NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
    name        TEXT      NOT NULL UNIQUE PRIMARY KEY,
    owner       TEXT      NOT NULL,
    world       TEXT      NULL,
    members     LIST      NOT NULL,
    last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (owner, world)
);