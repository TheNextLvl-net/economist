package net.thenextlvl.economist.controller.data;

import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SQLController implements DataController {
    private final Connection connection;

    public SQLController(Connection connection) throws SQLException {
        this.connection = connection;
        createTables();
    }

    @Override
    public boolean deleteAccount(String name) {
        try {
            executeUpdate("DELETE FROM accounts WHERE name = ?", name);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean deleteAccount(UUID uuid, @Nullable World world) {
        try {
            executeUpdate("DELETE FROM accounts WHERE uuid = ? AND world = ?",
                    uuid, world == null ? null : world.getName());
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private void createTables() throws SQLException {
        executeUpdate("""
                CREATE TABLE IF NOT EXISTS accounts (
                  uuid TEXT NOT NULL UNIQUE PRIMARY KEY,
                  balance DECIMAL(65, 30) NOT NULL,
                  world TEXT NULL
                )""");
        executeUpdate("""
                CREATE TABLE IF NOT EXISTS banks (
                  name TEXT NOT NULL UNIQUE PRIMARY KEY,
                  owner TEXT NOT NULL UNIQUE,
                  members LIST NOT NULL
                )""");
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    protected <T> T executeQuery(String query, ThrowingFunction<ResultSet, T> mapper, Object... parameters) throws SQLException {
        try (var preparedStatement = connection.prepareStatement(query)) {
            for (var i = 0; i < parameters.length; i++)
                preparedStatement.setObject(i + 1, parameters[i]);
            try (var resultSet = preparedStatement.executeQuery()) {
                return ThrowingFunction.unchecked(mapper).apply(resultSet);
            }
        }
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    protected void executeUpdate(String query, @Nullable Object... parameters) throws SQLException {
        try (var preparedStatement = connection.prepareStatement(query)) {
            for (var i = 0; i < parameters.length; i++)
                preparedStatement.setObject(i + 1, parameters[i]);
            preparedStatement.executeUpdate();
        }
    }

    @FunctionalInterface
    protected interface ThrowingFunction<T, R> {
        R apply(T t) throws SQLException;

        static <T, R> ThrowingFunction<T, R> unchecked(ThrowingFunction<T, R> f) {
            return f;
        }
    }
}
