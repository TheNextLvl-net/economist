package net.thenextlvl.economist.plugin.controller.data;

import de.chojo.sadu.core.databases.Database;
import de.chojo.sadu.sqlite.databases.SqLite;
import net.thenextlvl.economist.plugin.configuration.StorageType;

enum DatabaseDialect {
    SQLITE("sqlite", "jdbc:sqlite:", "org.sqlite.JDBC", 0, SqLiteDatabaseAdapter.INSTANCE, "sqlite"),
    POSTGRESQL("postgresql", "jdbc:postgresql://", "org.postgresql.Driver", 5432,
            new ResourceDatabase("postgresql", "jdbc:postgresql://", "org.postgresql.Driver", "postgres"), "postgres", "postgresql"),
    MYSQL("mysql", "jdbc:mysql://", "com.mysql.cj.jdbc.Driver", 3306,
            new ResourceDatabase("mysql", "jdbc:mysql://", "com.mysql.cj.jdbc.Driver"), "mysql"),
    MARIADB("mariadb", "jdbc:mariadb://", "org.mariadb.jdbc.Driver", 3306,
            new ResourceDatabase("mysql", "jdbc:mariadb://", "org.mariadb.jdbc.Driver", "mariadb"), "mariadb");

    private final String id;
    private final String jdbcPrefix;
    private final String defaultDriverClass;
    private final int defaultPort;
    private final Database<?, ?> database;
    private final String[] aliases;

    DatabaseDialect(
            final String id,
            final String jdbcPrefix,
            final String defaultDriverClass,
            final int defaultPort,
            final Database<?, ?> database,
            final String... aliases
    ) {
        this.id = id;
        this.jdbcPrefix = jdbcPrefix;
        this.defaultDriverClass = defaultDriverClass;
        this.defaultPort = defaultPort;
        this.database = database;
        this.aliases = aliases;
    }

    static DatabaseDialect fromStorageType(final StorageType storageType) {
        return switch (storageType) {
            case SQLite -> SQLITE;
            case PostgreSQL -> POSTGRESQL;
            case MariaDB -> MARIADB;
            case MySQL -> MYSQL;
        };
    }

    boolean matches(final String value) {
        if (id.equalsIgnoreCase(value)) return true;
        for (final var alias : aliases) {
            if (alias.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    String id() {
        return id;
    }

    String jdbcPrefix() {
        return jdbcPrefix;
    }

    String defaultDriverClass() {
        return defaultDriverClass;
    }

    int defaultPort() {
        return defaultPort;
    }

    Database<?, ?> database() {
        return database;
    }

    private static final class SqLiteDatabaseAdapter {
        private static final SqLite INSTANCE = SqLite.get();
    }
}
