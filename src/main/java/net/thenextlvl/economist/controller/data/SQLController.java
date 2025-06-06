package net.thenextlvl.economist.controller.data;

import net.kyori.adventure.key.Key;
import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.Account;
import net.thenextlvl.economist.model.EconomistAccount;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@NullMarked
public class SQLController implements DataController {
    private final Connection connection;
    private final EconomistPlugin plugin;

    public SQLController(Connection connection, EconomistPlugin plugin) throws SQLException {
        this.connection = connection;
        this.plugin = plugin;
        createAccountTable();
        createBankTable();
    }

    @Override
    public boolean deleteAccounts(List<UUID> accounts, @Nullable World world) throws SQLException {
        var name = world != null ? world.key().asString() : null;
        var statement = "DELETE FROM accounts WHERE uuid IN (" +
                        String.join(",", Collections.nCopies(accounts.size(), "?")) +
                        ") AND (world = ? OR (? IS NULL AND world IS NULL))";
        var params = new ArrayList<@Nullable Object>(accounts);
        params.add(name);
        params.add(name);
        return executeUpdate(statement, params.toArray(new Object[0])) != 0;
    }

    @Override
    public List<Account> getOrdered(@Nullable World world, int start, int limit) throws SQLException {
        var name = world != null ? world.key().asString() : null;
        var zero = plugin.config.balanceTop.showEmptyAccounts;
        return Objects.requireNonNull(executeQuery("""
                SELECT balance, uuid FROM accounts WHERE
                (world = ? OR (? IS NULL AND world IS NULL))
                """ + (zero ? "" : "AND balance != 0 ") + """
                ORDER BY balance DESC LIMIT ? OFFSET ?""", resultSet -> {
            var accounts = new LinkedList<Account>();
            while (resultSet.next()) {
                var balance = resultSet.getBigDecimal("balance");
                var owner = UUID.fromString(resultSet.getString("uuid"));
                accounts.add(new EconomistAccount(balance, world, owner));
            }
            return accounts;
        }, name, name, limit, start));
    }

    @Override
    public Account createAccount(UUID uuid, @Nullable World world) throws SQLException {
        var balance = plugin.config.startBalance;
        executeUpdate("INSERT INTO accounts (uuid, world, balance) VALUES (?, ?, ?)",
                uuid, world != null ? world.key().asString() : null, balance);
        return new EconomistAccount(BigDecimal.valueOf(balance), world, uuid);
    }

    @Override
    public BigDecimal getTotalBalance(@Nullable World world) throws SQLException {
        var name = world != null ? world.key().asString() : null;
        return Objects.requireNonNull(executeQuery("""
                SELECT SUM(balance) as total_balance FROM accounts WHERE (world = ? OR (? IS NULL AND world IS NULL))
                """, resultSet -> {
            if (!resultSet.next()) return BigDecimal.ZERO;
            return resultSet.getBigDecimal("total_balance");
        }, name, name));
    }

    @Override
    public Set<UUID> getAccountOwners(@Nullable World world) throws SQLException {
        var name = world != null ? world.key().asString() : null;
        return Objects.requireNonNull(executeQuery("""
                SELECT uuid FROM accounts WHERE (world = ? OR (? IS NULL AND world IS NULL))
                """, resultSet -> {
            var accounts = new HashSet<UUID>();
            while (resultSet.next()) {
                var owner = UUID.fromString(resultSet.getString("uuid"));
                accounts.add(owner);
            }
            return accounts;
        }, name, name));
    }

    @Override
    public @Nullable Account getAccount(UUID uuid, @Nullable World world) throws SQLException {
        var name = world != null ? world.key().asString() : null;
        return executeQuery("SELECT balance FROM accounts WHERE uuid = ? AND (world = ? OR (? IS NULL AND world IS NULL))",
                resultSet -> {
                    if (!resultSet.next()) return null;
                    var balance = resultSet.getBigDecimal("balance");
                    return balance != null ? new EconomistAccount(balance, world, uuid) : null;
                }, uuid, name, name);
    }

    @Override
    public Set<Account> getAccounts(@Nullable World world) throws SQLException {
        var name = world != null ? world.key().asString() : null;
        return Objects.requireNonNull(executeQuery("""
                SELECT uuid, balance FROM accounts WHERE (world = ? OR (? IS NULL AND world IS NULL))
                """, resultSet -> {
            var accounts = new HashSet<Account>();
            while (resultSet.next()) {
                var owner = UUID.fromString(resultSet.getString("uuid"));
                var balance = resultSet.getBigDecimal("balance");
                accounts.add(new EconomistAccount(balance, world, owner));
            }
            return accounts;
        }, name, name));
    }

    @Override
    public boolean save(Account account) throws SQLException {
        var name = account.getWorld().map(World::key).map(Key::asString).orElse(null);
        return executeUpdate("UPDATE accounts SET balance = ? WHERE uuid = ? AND (world = ? OR (? IS NULL AND world IS NULL))",
                account.getBalance(), account.getOwner(), name, name) == 1;
    }

    protected void createAccountTable() throws SQLException {
        executeUpdate("""
                CREATE TABLE IF NOT EXISTS accounts (
                  uuid TEXT NOT NULL,
                  balance DECIMAL(65, 20) NOT NULL,
                  world TEXT NULL,
                  UNIQUE (uuid, world)
                )""");

        executeUpdate("""
                CREATE TRIGGER IF NOT EXISTS enforce_unique_uuid_world
                BEFORE INSERT ON accounts
                FOR EACH ROW
                WHEN NEW.world IS NULL
                BEGIN
                  SELECT RAISE(ABORT, 'Cannot insert another row with NULL world for the same uuid')
                  WHERE EXISTS (
                    SELECT 1 FROM accounts WHERE uuid = NEW.uuid AND world IS NULL
                  );
                END;""");
        executeUpdate("""
                CREATE TRIGGER IF NOT EXISTS enforce_unique_uuid_world_update
                BEFORE UPDATE ON accounts
                FOR EACH ROW
                WHEN NEW.world IS NULL
                BEGIN
                  SELECT RAISE(ABORT, 'Cannot update to a row with NULL world for the same uuid')
                  WHERE EXISTS (
                    SELECT 1 FROM accounts WHERE uuid = NEW.uuid AND world IS NULL AND rowid != OLD.rowid
                  );
                END;""");
    }

    protected void createBankTable() throws SQLException {
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
    protected int executeUpdate(String query, @Nullable Object... parameters) throws SQLException {
        try (var preparedStatement = connection.prepareStatement(query)) {
            for (var i = 0; i < parameters.length; i++)
                preparedStatement.setObject(i + 1, parameters[i]);
            return preparedStatement.executeUpdate();
        }
    }

    @FunctionalInterface
    protected interface ThrowingFunction<T, R> {
        @Nullable
        R apply(T t) throws SQLException;

        static <T, R> ThrowingFunction<T, R> unchecked(ThrowingFunction<T, R> f) {
            return f;
        }
    }
}
