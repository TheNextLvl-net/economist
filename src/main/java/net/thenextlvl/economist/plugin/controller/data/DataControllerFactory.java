package net.thenextlvl.economist.plugin.controller.data;

import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.configuration.PluginConfig;

import java.io.File;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public final class DataControllerFactory {
    private DataControllerFactory() {
    }

    public static DataController create(final EconomistPlugin plugin) throws SQLException {
        final var dialect = DatabaseDialect.fromStorageType(plugin.config.database.storageType);
        if (dialect == DatabaseDialect.SQLITE) return new SQLiteController(plugin);
        return new RemoteSQLController(createRemoteDataSource(plugin.config.database, dialect, plugin), plugin, dialect);
    }

    private static DriverManagerDataSource createRemoteDataSource(
            final PluginConfig.DatabaseConfig config,
            final DatabaseDialect dialect,
            final EconomistPlugin plugin
    ) throws SQLException {
        final var jdbcUrl = resolveJdbcUrl(config, dialect, plugin);
        // loadDriver(config, dialect); // todo: can be skipped?
        final var username = config.username.isBlank() ? null : config.username;
        final var password = config.password.isBlank() ? null : config.password;
        return new DriverManagerDataSource(jdbcUrl, username, password);
    }

    private static void loadDriver(final PluginConfig.DatabaseConfig config, final DatabaseDialect dialect) throws SQLException {
        try {
            Class.forName(dialect.defaultDriverClass());
        } catch (final ClassNotFoundException exception) {
            throw new SQLException("Database driver not found: " + dialect.defaultDriverClass(), exception);
        }
    }

    private static String resolveJdbcUrl(
            final PluginConfig.DatabaseConfig config,
            final DatabaseDialect dialect,
            final EconomistPlugin plugin
    ) {
        if (dialect == DatabaseDialect.SQLITE) {
            return "jdbc:sqlite:" + new File(plugin.getDataFolder(), "saves.db");
        }
        if (!config.jdbcUrl.isBlank()) {
            return config.jdbcUrl;
        }
        final var host = config.host.isBlank() ? "localhost" : config.host;
        final var port = config.port > 0 ? config.port : dialect.defaultPort();
        final var database = config.database.isBlank() ? "economist" : config.database;
        return dialect.jdbcPrefix() + host + ":" + port + "/" + database;
    }

    private static String appendProperties(final String jdbcUrl, final Map<String, String> properties) {
        if (properties.isEmpty()) return jdbcUrl;
        final var separator = jdbcUrl.contains("?") ? "&" : "?";
        final var joiner = new StringJoiner("&", jdbcUrl + separator, "");
        final var sorted = new LinkedHashMap<>(properties);
        sorted.forEach((key, value) -> joiner.add(key + "=" + value));
        return joiner.toString();
    }
}
