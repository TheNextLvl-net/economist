package net.thenextlvl.economist.controller.data;

import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.Account;
import net.thenextlvl.economist.api.currency.Currency;
import net.thenextlvl.economist.model.EconomistAccount;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.sqlite.SQLiteErrorCode;

import java.io.File;
import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@NullMarked
public class SQLiteController extends SQLController {
    private static final String CREATE_ACCOUNT = statement("sql/update/create_account.sql");
    private static final String DELETE_ACCOUNT = statement("sql/update/delete_account.sql");
    private static final String PRUNE_ACCOUNTS = statement("sql/update/prune.sql");
    private static final String UPSERT_BALANCES = statement("sql/update/upsert_balances.sql");

    private static final String GET_ACCOUNTS = statement("sql/query/get_accounts.sql");
    private static final String GET_BALANCES = statement("sql/query/get_balances.sql");
    private static final String LIST_ORDERED = statement("sql/query/list_ordered.sql");
    private static final String TOTAL_BALANCE = statement("sql/query/total_balance.sql");

    public SQLiteController(EconomistPlugin plugin) throws SQLException {
        super(DriverManager.getConnection("jdbc:sqlite:" + new File(plugin.getDataFolder(), "saves.db")), plugin);
    }

    @Override
    public @Nullable Account getAccount(UUID uuid, @Nullable World world) {
        try {
            return executeQuery(GET_BALANCES, resultSet -> {
                var currencies = new HashMap<String, BigDecimal>();
                while (resultSet.next()) {
                    var balance = resultSet.getBigDecimal("balance");
                    var currency = resultSet.getString("currency");
                    currencies.put(currency, balance);
                }
                if (currencies.isEmpty()) return null;
                return new EconomistAccount(plugin, currencies, world, uuid);
            }, uuid, world != null ? world.key().asString() : null);
        } catch (SQLException e) {
            plugin.getComponentLogger().error("Failed to get account", e);
            return null;
        }
    }

    @Override
    public @Unmodifiable Set<Account> getAccounts(@Nullable World world) {
        try {
            return executeQuery(GET_ACCOUNTS, resultSet -> {
                var accounts = new HashMap<String, HashMap<String, BigDecimal>>();
                while (resultSet.next()) {
                    var uuid = resultSet.getString("uuid");
                    var balance = resultSet.getBigDecimal("balance");
                    var currency = resultSet.getString("currency");
                    accounts.computeIfAbsent(uuid, ignored -> new HashMap<>()).put(currency, balance);
                }
                return accounts.entrySet().stream().map(entry -> {
                    var uuid = UUID.fromString(entry.getKey());
                    return new EconomistAccount(plugin, entry.getValue(), world, uuid);
                }).collect(Collectors.toUnmodifiableSet());
            }, world != null ? world.key().asString() : null);
        } catch (SQLException e) {
            plugin.getComponentLogger().error("Failed to get accounts", e);
            return Set.of();
        }
    }

    @Override
    public Account createAccount(UUID uuid, @Nullable World world) {
        try {
            executeUpdate(CREATE_ACCOUNT, uuid, world != null ? world.key().asString() : null);
            return new EconomistAccount(plugin, world, uuid);
        } catch (SQLException e) {
            if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE.code) {
                throw new IllegalStateException("Account already exists for UUID " + uuid + " in world " + world, e);
            }
            plugin.getComponentLogger().error("Failed to create account", e);
            throw new RuntimeException("Failed to create account", e);
        }
    }

    @Override
    public BigDecimal getTotalBalance(Currency currency, @Nullable World world) {
        try {
            return executeQuery(TOTAL_BALANCE, resultSet -> {
                if (!resultSet.next()) return BigDecimal.ZERO;
                return resultSet.getBigDecimal("total_balance");
            }, world != null ? world.key().asString() : null);
        } catch (SQLException e) {
            plugin.getComponentLogger().error("Failed to get total balance", e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public List<Account> getOrdered(Currency currency, @Nullable World world, int offset, int limit) {
        try {
            var worldName = world != null ? world.key().asString() : null;
            var emptyAccounts = plugin.config.balanceTop.showEmptyAccounts;
            return executeQuery(LIST_ORDERED, resultSet -> {
                var accounts = new LinkedList<Account>();
                while (resultSet.next()) {
                    var balance = resultSet.getBigDecimal("balance");
                    var owner = UUID.fromString(resultSet.getString("uuid"));
                    var account = new EconomistAccount(plugin, world, owner);
                    account.setBalance(balance, currency);
                    accounts.add(account);
                }
                return accounts;
            }, currency.getName(), worldName, emptyAccounts, limit, offset);
        } catch (SQLException e) {
            plugin.getComponentLogger().error("Failed to load accounts ordered", e);
            return List.of();
        }
    }

    @Override
    public int prune(Duration duration, @Nullable World world) {
        try {
            return executeUpdate(PRUNE_ACCOUNTS, world != null ? world.key().asString() : null, duration.toSeconds());
        } catch (SQLException e) {
            plugin.getComponentLogger().error("Failed to prune accounts", e);
            return 0;
        }
    }

    @Override
    public boolean deleteAccount(UUID uuid, @Nullable World world) {
        try {
            executeUpdate(DELETE_ACCOUNT, uuid, world != null ? world.key().asString() : null);
            return true;
        } catch (SQLException e) {
            plugin.getComponentLogger().error("Failed to delete account {}", uuid, e);
            return false;
        }
    }

    @Override
    public boolean save(Account account) {
        try {
            var balances = ((EconomistAccount) account).getBalances();
            if (balances.isEmpty()) return true;

            var batches = balances.entrySet().stream().map(entry -> new Object[]{
                    account.getOwner(), entry.getKey(), entry.getValue()
            }).toList();

            for (int result : executeBatchUpdate(UPSERT_BALANCES, batches)) {
                if (result >= 0 || result == Statement.SUCCESS_NO_INFO) continue;
                plugin.getComponentLogger().error("Failed to save some balances for account {}", account);
                return false;
            }
            return true;
        } catch (SQLException e) {
            plugin.getComponentLogger().error("Failed to save account {}", account.getOwner(), e);
            return false;
        }
    }

    @Override
    protected void setupDatabase() throws SQLException {
        executeUpdate(statement("sql/table/accounts.sql"));
        executeUpdate(statement("sql/table/banks.sql"));
        executeUpdate(statement("sql/trigger/enforce_unique_account_uuid_world_insert.sql"));
        executeUpdate(statement("sql/trigger/enforce_unique_account_uuid_world_update.sql"));
        executeUpdate(statement("sql/index/balances_id_currency.sql"));
    }

    @Override
    protected void migrateDatabase() throws SQLException {
        migrateV01ToV1();
    }

    private void migrateV01ToV1() throws SQLException {
        if (executeQuery(statement("sql/migration/v0.1_to_v1_required.sql"), resultSet -> {
            return !resultSet.next() || resultSet.getInt(1) == 0;
        })) return;
        plugin.getComponentLogger().info("Migrating database from v0.1 to v1...");
        executeUpdate(statement("sql/migration/v0.1_to_v1.sql"));
        plugin.getComponentLogger().info("Migration complete");
    }
}
