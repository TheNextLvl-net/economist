package net.thenextlvl.economist.configuration;

import org.jspecify.annotations.NullMarked;

@NullMarked
public enum StorageType {
    PostgreSQL,
    MongoDB,
    SQLite,
    MySQL
}
