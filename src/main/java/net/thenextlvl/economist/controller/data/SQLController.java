package net.thenextlvl.economist.controller.data;

import net.kyori.adventure.key.Key;
import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.Account;
import net.thenextlvl.economist.api.currency.Currency;
import net.thenextlvl.economist.model.EconomistAccount;
import org.bukkit.World;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class SQLController implements AutoCloseable, DataController {
    private final @NonNull Connection connection;
    private final @NonNull EconomistPlugin plugin;

    public SQLController(@NonNull Connection connection, @NonNull EconomistPlugin plugin) {
        this.connection = connection;
        this.plugin = plugin;
    }
    
    protected abstract void setupDatabase() throws SQLException;

    @Override
    public @NonNull Account createAccount(@NonNull UUID uuid, @Nullable World world) throws SQLException {
        var balance = plugin.config.startBalance;
        executeUpdate("INSERT INTO accounts (uuid, world, balance) VALUES (?, ?, ?)",
                uuid, world != null ? world.key().asString() : null, balance);
        // todo: initialize balances
        return new EconomistAccount(world, uuid);
    }

    @Override
    public @NonNull BigDecimal getTotalBalance(@NonNull Currency currency, @Nullable World world) throws SQLException {
        // todo: respect currency
        var name = world != null ? world.key().asString() : null;
        return executeQuery("""
                SELECT SUM(balance) as total_balance FROM accounts WHERE (world = ? OR (? IS NULL AND world IS NULL))
                """, resultSet -> {
            if (!resultSet.next()) return BigDecimal.ZERO;
            return resultSet.getBigDecimal("total_balance");
        }, name, name);
    }

    @Override
    public @NonNull List<Account> getOrdered(@NonNull Currency currency, @Nullable World world, int start, int limit) throws SQLException {
        // todo: respect currency
        var name = world != null ? world.key().asString() : null;
        var zero = plugin.config.balanceTop.showEmptyAccounts;
        return executeQuery("""
                                    SELECT balance, uuid FROM accounts WHERE
                                    (world = ? OR (? IS NULL AND world IS NULL))
                                    """ + (zero ? "" : "AND balance != 0 ") + """
                                    ORDER BY balance DESC LIMIT ? OFFSET ?""", resultSet -> {
            var accounts = new LinkedList<Account>();
            while (resultSet.next()) {
                var balance = resultSet.getBigDecimal("balance");
                var owner = UUID.fromString(resultSet.getString("uuid"));
                // todo: initialize balances
                accounts.add(new EconomistAccount(world, owner));
            }
            return accounts;
        }, name, name, limit, start);
    }

    @Override
    public @Nullable Account getAccount(@NonNull UUID uuid, @Nullable World world) throws SQLException {
        var name = world != null ? world.key().asString() : null;
        return executeQuery("SELECT balance FROM accounts WHERE uuid = ? AND (world = ? OR (? IS NULL AND world IS NULL))",
                resultSet -> {
                    if (!resultSet.next()) return null;
                    var balance = resultSet.getBigDecimal("balance");
                    // fixme: initialize balance
                    return balance != null ? new EconomistAccount(world, uuid) : null;
                }, uuid, name, name);
    }

    @Override
    public @NonNull Set<Account> getAccounts(@Nullable World world) throws SQLException {
        var name = world != null ? world.key().asString() : null;
        return executeQuery("""
                SELECT uuid, balance FROM accounts WHERE (world = ? OR (? IS NULL AND world IS NULL))
                """, resultSet -> {
            var accounts = new HashSet<Account>();
            while (resultSet.next()) {
                var owner = UUID.fromString(resultSet.getString("uuid"));
                var balance = resultSet.getBigDecimal("balance");
                // fixme: initialize balance
                accounts.add(new EconomistAccount(world, owner));
            }
            return accounts;
        }, name, name);
    }

    @Override
    public boolean save(@NonNull Account account) throws SQLException {
        var name = account.getWorld().map(World::key).map(Key::asString).orElse(null);
        return executeUpdate("UPDATE accounts SET balance = ? WHERE uuid = ? AND (world = ? OR (? IS NULL AND world IS NULL))",
                // fixme: save balances
                account.getBalance(null), account.getOwner(), name, name) == 1;
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    protected <T> T executeQuery(@NonNull String sql, @NonNull ThrowingFunction<ResultSet, T> mapper, Object... parameters) throws SQLException {
        try (var preparedStatement = connection.prepareStatement(sql)) {
            for (var i = 0; i < parameters.length; i++)
                preparedStatement.setObject(i + 1, parameters[i]);
            try (var resultSet = preparedStatement.executeQuery()) {
                return mapper.apply(resultSet);
            }
        }
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    protected int executeUpdateGetKey(String sql, @Nullable Object... parameters) throws SQLException {
        try (var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (var i = 0; i < parameters.length; i++) statement.setObject(i + 1, parameters[i]);
            if (statement.executeUpdate() == 0) throw new SQLException("No rows affected");
            try (var generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) return generatedKeys.getInt(1);
            }
            throw new SQLException("Statement returns no generated keys");
        }
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    protected int executeUpdate(@NonNull String sql, Object... parameters) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            for (var i = 0; i < parameters.length; i++) statement.setObject(i + 1, parameters[i]);
            return statement.executeUpdate();
        }
    }

    protected static @NonNull String statement(@NonNull String file) {
        try (var resource = SQLController.class.getClassLoader().getResourceAsStream(file)) {
            if (resource == null) throw new FileNotFoundException("Resource not found: " + file);
            try (var reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @FunctionalInterface
    protected interface ThrowingFunction<T, R> {
        @Nullable
        R apply(@NonNull T t) throws SQLException;
    }
}
