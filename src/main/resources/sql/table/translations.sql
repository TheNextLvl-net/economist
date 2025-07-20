CREATE TABLE IF NOT EXISTS translations
(
    name     TEXT NOT NULL,
    singular TEXT NOT NULL,
    plural   TEXT NOT NULL,
    locale   TEXT NOT NULL,
    UNIQUE (name, locale)
);