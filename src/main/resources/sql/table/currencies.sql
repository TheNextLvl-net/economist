CREATE TABLE IF NOT EXISTS currencies
(
    name          TEXT PRIMARY KEY NOT NULL,
    symbol        TEXT             NOT NULL,
    fractions     INTEGER          NOT NULL,
    display_names TEXT             NOT NULL,
    FOREIGN KEY (display_names) REFERENCES translations (name) ON DELETE CASCADE
);