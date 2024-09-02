package net.thenextlvl.economist.controller.data;

import lombok.SneakyThrows;
import net.kyori.adventure.key.Key;
import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.Account;
import net.thenextlvl.economist.model.EconomistAccount;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SQLController implements DataController {
    private final Connection connection;
    private final EconomistPlugin plugin;

    public SQLController(Connection connection, EconomistPlugin plugin) throws SQLException {
        this.connection = connection;
        this.plugin = plugin;
        createTables();
    }

    @Override
    public boolean deleteAccount(UUID uuid, @Nullable World world) {
        try {
            var name = world != null ? world.key().asString() : null;
            executeUpdate("DELETE FROM accounts WHERE uuid = ? AND (world = ? OR (? IS NULL AND world IS NULL))",
                    uuid, name, name);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public Account createAccount(UUID uuid, @Nullable World world) {
        try {
            var balance = plugin.config().startBalance();
            executeUpdate("INSERT INTO accounts (uuid, world, balance) VALUES (?, ?, ?)",
                    uuid, world != null ? world.key().asString() : null, balance);
            return new EconomistAccount(balance, world, uuid);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SneakyThrows
    public @Nullable Account getAccount(UUID uuid, @Nullable World world) {
        var name = world != null ? world.key().asString() : null;
        return executeQuery("SELECT * FROM accounts WHERE uuid = ? AND (world = ? OR (? IS NULL AND world IS NULL))",
                resultSet -> {
                    if (!resultSet.next()) return null;
                    var balance = resultSet.getBigDecimal("balance");
                    return balance != null ? new EconomistAccount(balance, world, uuid) : null;
                }, uuid, name, name);
    }

    @Override
    public boolean save(Account account) {
        try {
            var name = account.getWorld().map(World::key).map(Key::asString).orElse(null);
            executeUpdate("UPDATE accounts SET balance = ? WHERE uuid = ? AND (world = ? OR (? IS NULL AND world IS NULL))",
                    account.getBalance(), account.getOwner(), name, name);
            return true;
        } catch (SQLException e) {
            plugin.getComponentLogger().error("Failed to save account {}", account.getOwner(), e);
            return false;
        }
    }

    private void createTables() throws SQLException {
        executeUpdate("""
                CREATE TABLE IF NOT EXISTS accounts (
                  uuid TEXT NOT NULL UNIQUE PRIMARY KEY,
                  balance DECIMAL(65, 20) NOT NULL,
                  world TEXT NULL
                )""");
        executeUpdate("""
                CREATE TABLE IF NOT EXISTS banks (
                  name TEXT NOT NULL UNIQUE PRIMARY KEY,
                  balance DECIMAL(65, 20) NOT NULL,
                  owner TEXT NOT NULL UNIQUE,
                  members LIST NOT NULL
                )""");
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    protected <T> @Nullable T executeQuery(String query, ThrowingFunction<ResultSet, T> mapper, @Nullable Object... parameters) throws SQLException {
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
        @Nullable R apply(T t) throws SQLException;

        static <T, R> ThrowingFunction<T, R> unchecked(ThrowingFunction<T, R> f) {
            return f;
        }
    }
}
