package net.thenextlvl.economist.plugin.controller.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.chojo.sadu.core.databases.Database;
import de.chojo.sadu.core.updater.SqlVersion;
import de.chojo.sadu.updater.BaseSqlUpdaterBuilder;
import de.chojo.sadu.updater.SqlUpdater;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.thenextlvl.economist.Account;
import net.thenextlvl.economist.bank.Bank;
import net.thenextlvl.economist.currency.Currency;
import net.thenextlvl.economist.currency.CurrencyData;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.model.EconomistAccount;
import net.thenextlvl.economist.plugin.model.EconomistBank;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SQLController implements DataController {
    private static final SqlVersion SCHEMA_VERSION = new SqlVersion(1, 0);
    private static final String VERSION_TABLE = "economist_schema_version";
    private static final Gson GSON = new Gson();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final DataSource dataSource;
    private final DatabaseDialect dialect;
    protected final EconomistPlugin plugin;

    protected SQLController(final DataSource dataSource, final EconomistPlugin plugin,
                            final DatabaseDialect dialect, final Database<?, ?> database) throws SQLException {
        this.dataSource = dataSource;
        this.dialect = dialect;
        this.plugin = plugin;
        migrate(database);
    }

    @Override
    public boolean deleteAccounts(final List<UUID> accounts, @Nullable final World world) throws SQLException {
        if (accounts.isEmpty()) return false;
        final var worldName = worldName(world);
        final var statement = "DELETE FROM accounts WHERE uuid IN (" +
                String.join(",", Collections.nCopies(accounts.size(), "?")) +
                ") AND (world = ? OR (? IS NULL AND world IS NULL))";
        final var parameters = new ArrayList<>(accounts.size() + 2);
        parameters.addAll(accounts);
        parameters.add(worldName);
        parameters.add(worldName);
        return executeUpdate(statement, parameters.toArray()) > 0;
    }

    @Override
    public List<Account> getOrdered(@Nullable final World world, final int start, final int limit) throws SQLException {
        return getOrdered(defaultCurrency(), world, start, limit);
    }

    @Override
    public List<Account> getOrdered(final Currency currency, @Nullable final World world, final int start, final int limit) throws SQLException {
        final var worldName = worldName(world);
        final var statement = """
                SELECT base.uuid, base.world
                FROM (
                    SELECT DISTINCT uuid, world
                    FROM accounts
                    WHERE (world = ? OR (? IS NULL AND world IS NULL))
                ) base
                LEFT JOIN accounts currency_balance
                    ON currency_balance.uuid = base.uuid
                    AND ((currency_balance.world = base.world) OR (currency_balance.world IS NULL AND base.world IS NULL))
                    AND currency_balance.currency = ?
                """ + (plugin.config.pagination.showEmptyAccounts ? "" : "WHERE COALESCE(currency_balance.balance, 0) <> 0\n") + """
                ORDER BY COALESCE(currency_balance.balance, 0) DESC
                LIMIT ? OFFSET ?
                """;
        final var keys = executeQuery(statement, resultSet -> {
            final var entries = new LinkedList<AccountKey>();
            while (resultSet.next()) {
                entries.add(new AccountKey(
                        UUID.fromString(resultSet.getString("uuid")),
                        resultSet.getString("world")
                ));
            }
            return entries;
        }, worldName, worldName, currency.getName(), limit, start);
        return keys == null || keys.isEmpty() ? List.of() : loadAccounts(keys, world);
    }

    @Override
    public Account createAccount(final UUID uuid, @Nullable final World world) throws SQLException {
        final var balances = new ConcurrentHashMap<String, BigDecimal>();
        final var currency = defaultCurrency();
        final var balance = currency.getStarterBalance();
        balances.put(currency.getName(), balance);
        executeUpdate("INSERT INTO accounts (uuid, world, currency, balance) VALUES (?, ?, ?, ?)",
                uuid, worldName(world), currency.getName(), balance);
        return new EconomistAccount(uuid, world, balances);
    }

    @Override
    public BigDecimal getTotalBalance(@Nullable final World world) throws SQLException {
        return getTotalBalance(defaultCurrency(), world);
    }

    @Override
    public BigDecimal getTotalBalance(final Currency currency, @Nullable final World world) throws SQLException {
        final var worldName = worldName(world);
        final var total = executeQuery("""
                        SELECT SUM(balance) AS total_balance
                        FROM accounts
                        WHERE currency = ? AND (world = ? OR (? IS NULL AND world IS NULL))
                        """, resultSet -> resultSet.next() ? resultSet.getBigDecimal("total_balance") : BigDecimal.ZERO,
                currency.getName(), worldName, worldName);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public Set<UUID> getAccountOwners(@Nullable final World world) throws SQLException {
        final var worldName = worldName(world);
        return Objects.requireNonNull(executeQuery("""
                SELECT DISTINCT uuid FROM accounts
                WHERE (world = ? OR (? IS NULL AND world IS NULL))
                """, resultSet -> {
            final var accounts = new HashSet<UUID>();
            while (resultSet.next()) {
                accounts.add(UUID.fromString(resultSet.getString("uuid")));
            }
            return accounts;
        }, worldName, worldName));
    }

    @Override
    public @Nullable Account getAccount(final UUID uuid, @Nullable final World world) throws SQLException {
        final var worldName = worldName(world);
        return executeQuery("""
                SELECT currency, balance FROM accounts
                WHERE uuid = ? AND (world = ? OR (? IS NULL AND world IS NULL))
                """, resultSet -> {
            final var balances = new ConcurrentHashMap<String, BigDecimal>();
            while (resultSet.next()) {
                balances.put(resultSet.getString("currency"), resultSet.getBigDecimal("balance"));
            }
            return balances.isEmpty() ? null : new EconomistAccount(uuid, world, balances);
        }, uuid, worldName, worldName);
    }

    @Override
    public Set<Account> getAccounts(@Nullable final World world) throws SQLException {
        final var worldName = worldName(world);
        final var accounts = executeQuery("""
                SELECT uuid, currency, balance
                FROM accounts
                WHERE (world = ? OR (? IS NULL AND world IS NULL))
                ORDER BY uuid
                """, resultSet -> {
            final var loaded = new HashMap<UUID, ConcurrentHashMap<String, BigDecimal>>();
            while (resultSet.next()) {
                final var owner = UUID.fromString(resultSet.getString("uuid"));
                loaded.computeIfAbsent(owner, ignored -> new ConcurrentHashMap<>())
                        .put(resultSet.getString("currency"), resultSet.getBigDecimal("balance"));
            }
            final var values = new HashSet<Account>();
            loaded.forEach((owner, balances) -> values.add(new EconomistAccount(owner, world, balances)));
            return values;
        }, worldName, worldName);
        return accounts != null ? accounts : Set.of();
    }

    @Override
    public boolean save(final Account account) throws SQLException {
        final var balances = extractBalances(account);
        final var worldName = account.getWorld().map(World::key).map(Key::asString).orElse(null);
        try (final var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (final var delete = connection.prepareStatement("""
                        DELETE FROM accounts
                        WHERE uuid = ? AND (world = ? OR (? IS NULL AND world IS NULL))
                        """)) {
                    bind(delete, account.getOwner(), worldName, worldName);
                    delete.executeUpdate();
                }
                try (final var insert = connection.prepareStatement("""
                        INSERT INTO accounts (uuid, world, currency, balance)
                        VALUES (?, ?, ?, ?)
                        """)) {
                    for (final var entry : balances.entrySet()) {
                        bind(insert, account.getOwner(), worldName, entry.getKey(), entry.getValue());
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
                connection.commit();
                return true;
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public @Nullable Bank getBank(final String name) throws SQLException {
        return executeQuery("""
                SELECT name, balance, owner, members
                FROM banks
                WHERE name = ?
                """, resultSet -> resultSet.next() ? mapBank(resultSet) : null, name);
    }

    @Override
    public @Nullable Bank getBank(final UUID owner) throws SQLException {
        return executeQuery("""
                SELECT name, balance, owner, members
                FROM banks
                WHERE owner = ?
                """, resultSet -> resultSet.next() ? mapBank(resultSet) : null, owner);
    }

    @Override
    public Set<Bank> getBanks() throws SQLException {
        final var banks = executeQuery("""
                SELECT name, balance, owner, members
                FROM banks
                ORDER BY name
                """, resultSet -> {
            final var values = new LinkedHashMap<String, Bank>();
            while (resultSet.next()) {
                final var bank = mapBank(resultSet);
                values.put(bank.getName(), bank);
            }
            return Set.copyOf(values.values());
        });
        return banks != null ? banks : Set.of();
    }

    @Override
    public Bank createBank(final UUID owner, final String name) throws SQLException {
        final var balance = defaultCurrency().getStarterBalance();
        executeUpdate("""
                INSERT INTO banks (name, balance, owner, members)
                VALUES (?, ?, ?, ?)
                """, name, balance, owner, "[]");
        return new EconomistBank(name, owner, defaultCurrency().getName(), balance, Set.of());
    }

    @Override
    public boolean deleteBank(final String name) throws SQLException {
        return executeUpdate("DELETE FROM banks WHERE name = ?", name) > 0;
    }

    @Override
    public boolean deleteBank(final UUID owner) throws SQLException {
        return executeUpdate("DELETE FROM banks WHERE owner = ?", owner) > 0;
    }

    @Override
    public boolean save(final Bank bank) throws SQLException {
        return executeUpdate("""
                UPDATE banks
                SET balance = ?, owner = ?, members = ?
                WHERE name = ?
                """, bank.getBalance(defaultCurrency()), bank.getOwner(), serializeMembers(bank.getMembers()), bank.getName()) > 0;
    }

    @Override
    public Map<String, StoredCurrency> getCurrencies() throws SQLException {
        final var currencies = executeQuery("""
                SELECT name, symbol, fractional_digits, min_balance, max_balance, starter_balance
                FROM currencies
                ORDER BY name
                """, resultSet -> {
            final var values = new LinkedHashMap<String, StoredCurrency>();
            while (resultSet.next()) {
                final var name = resultSet.getString("name");
                final var data = CurrencyData.of(
                        name,
                        MINI_MESSAGE.deserialize(resultSet.getString("symbol")),
                        resultSet.getInt("fractional_digits")
                );
                values.put(name, new StoredCurrency(
                        data,
                        resultSet.getBigDecimal("min_balance"),
                        resultSet.getBigDecimal("max_balance"),
                        resultSet.getBigDecimal("starter_balance")
                ));
            }
            return values;
        });
        if (currencies == null || currencies.isEmpty()) return Map.of();
        executeQuery("""
                SELECT currency_name, locale, form, display_name
                FROM currency_translations
                ORDER BY currency_name, locale, form
                """, resultSet -> {
            while (resultSet.next()) {
                final var currency = currencies.get(resultSet.getString("currency_name"));
                if (currency == null) continue;
                final var locale = Locale.forLanguageTag(resultSet.getString("locale"));
                final var component = MINI_MESSAGE.deserialize(resultSet.getString("display_name"));
                switch (resultSet.getString("form")) {
                    case "singular" -> currency.data().displayNameSingular(locale, component);
                    case "plural" -> currency.data().displayNamePlural(locale, component);
                }
            }
            return null;
        });
        return currencies;
    }

    @Override
    public @Nullable String getDefaultCurrencyName() throws SQLException {
        return executeQuery("""
                SELECT default_currency
                FROM currency_settings
                WHERE id = 1
                """, resultSet -> resultSet.next() ? resultSet.getString("default_currency") : null);
    }

    @Override
    public boolean setDefaultCurrency(final String name) throws SQLException {
        return executeUpdate("""
                UPDATE currency_settings
                SET default_currency = ?
                WHERE id = 1
                """, name) > 0;
    }

    @Override
    public boolean save(final Currency currency) throws SQLException {
        try (final var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (final var upsert = connection.prepareStatement(currencyUpsertQuery())) {
                    bind(upsert,
                            currency.getName(),
                            MINI_MESSAGE.serialize(currency.getSymbol()),
                            currency.getFractionalDigits(),
                            currency.getMinBalance().orElse(null),
                            currency.getMaxBalance().orElse(null),
                            currency.getStarterBalance()
                    );
                    upsert.executeUpdate();
                }
                try (final var delete = connection.prepareStatement("""
                        DELETE FROM currency_translations
                        WHERE currency_name = ?
                        """)) {
                    bind(delete, currency.getName());
                    delete.executeUpdate();
                }
                try (final var insert = connection.prepareStatement(currencyTranslationInsertQuery())) {
                    bindDisplayNames(insert, currency.getName(), "singular", currency.toData().displayNamesSingular());
                    bindDisplayNames(insert, currency.getName(), "plural", currency.toData().displayNamesPlural());
                    insert.executeBatch();
                }
                connection.commit();
                return true;
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public boolean deleteCurrency(final String name) throws SQLException {
        try (final var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (final var deleteTranslations = connection.prepareStatement("""
                        DELETE FROM currency_translations
                        WHERE currency_name = ?
                        """)) {
                    bind(deleteTranslations, name);
                    deleteTranslations.executeUpdate();
                }
                final int deleted;
                try (final var deleteCurrency = connection.prepareStatement("DELETE FROM currencies WHERE name = ?")) {
                    bind(deleteCurrency, name);
                    deleted = deleteCurrency.executeUpdate();
                }
                connection.commit();
                return deleted > 0;
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    protected <T> @Nullable T executeQuery(final String query, final ThrowingFunction<ResultSet, T> mapper, @Nullable final Object... parameters) throws SQLException {
        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(query)) {
            bind(preparedStatement, parameters);
            try (final var resultSet = preparedStatement.executeQuery()) {
                return ThrowingFunction.unchecked(mapper).apply(resultSet);
            }
        }
    }

    protected int executeUpdate(final String query, @Nullable final Object... parameters) throws SQLException {
        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(query)) {
            bind(preparedStatement, parameters);
            return preparedStatement.executeUpdate();
        }
    }

    protected DataSource dataSource() {
        return dataSource;
    }

    private void migrate(final Database<?, ?> database) throws SQLException {
        try {
            final var builder = (BaseSqlUpdaterBuilder<?, ?>) SqlUpdater.builder(dataSource, database);
            builder.setVersion(SCHEMA_VERSION);
            builder.setVersionTable(VERSION_TABLE);
            builder.withClassLoader(plugin.getClass().getClassLoader());
            builder.execute();
        } catch (final IOException exception) {
            throw new SQLException("Failed to load database migration resources", exception);
        }
    }

    private static boolean tableExists(final DatabaseMetaData metaData, final String table) throws SQLException {
        try (final var tables = metaData.getTables(null, null, null, new String[]{"TABLE"})) {
            while (tables.next()) {
                if (table.equalsIgnoreCase(tables.getString("TABLE_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Account> loadAccounts(final List<AccountKey> keys, @Nullable final World world) throws SQLException {
        final var statement = new StringBuilder("""
                SELECT uuid, world, currency, balance
                FROM accounts
                WHERE
                """);
        final var parameters = new ArrayList<>();
        for (var i = 0; i < keys.size(); i++) {
            if (i > 0) statement.append(" OR ");
            statement.append("(uuid = ? AND (world = ? OR (? IS NULL AND world IS NULL)))");
            final var key = keys.get(i);
            parameters.add(key.uuid());
            parameters.add(key.world());
            parameters.add(key.world());
        }
        statement.append(" ORDER BY uuid");
        final var accounts = executeQuery(statement.toString(), resultSet -> {
            final var loaded = new LinkedHashMap<AccountKey, ConcurrentHashMap<String, BigDecimal>>();
            while (resultSet.next()) {
                final var key = new AccountKey(
                        UUID.fromString(resultSet.getString("uuid")),
                        resultSet.getString("world")
                );
                loaded.computeIfAbsent(key, ignored -> new ConcurrentHashMap<>())
                        .put(resultSet.getString("currency"), resultSet.getBigDecimal("balance"));
            }
            final var values = new ArrayList<Account>(keys.size());
            for (final var key : keys) {
                final var balances = loaded.get(key);
                if (balances != null) {
                    values.add(new EconomistAccount(key.uuid(), world, balances));
                }
            }
            return values;
        }, parameters.toArray());
        return accounts != null ? accounts : List.of();
    }

    private Map<String, BigDecimal> extractBalances(final Account account) {
        if (account instanceof final EconomistAccount economistAccount) {
            return new LinkedHashMap<>(economistAccount.balances());
        }
        throw new IllegalArgumentException("Unsupported account implementation: " + account.getClass().getName());
    }

    private Currency defaultCurrency() {
        return plugin.currencyController().getDefaultCurrency();
    }

    private Bank mapBank(final ResultSet resultSet) throws SQLException {
        return new EconomistBank(
                resultSet.getString("name"),
                UUID.fromString(resultSet.getString("owner")),
                defaultCurrency().getName(),
                resultSet.getBigDecimal("balance"),
                parseMembers(resultSet.getString("members"))
        );
    }

    private Set<UUID> parseMembers(final String members) {
        final var listType = new TypeToken<List<String>>() {
        }.getType();
        final List<String> values = GSON.fromJson(members, listType);
        if (values == null) return Set.of();
        return values.stream().map(UUID::fromString).collect(Collectors.toSet());
    }

    private String serializeMembers(final Set<UUID> members) {
        return GSON.toJson(members.stream().map(UUID::toString).toList());
    }

    private String currencyUpsertQuery() {
        return switch (dialect) {
            case SQLITE, POSTGRESQL -> """
                    INSERT INTO currencies (name, symbol, fractional_digits, min_balance, max_balance, starter_balance)
                    VALUES (?, ?, ?, ?, ?, ?)
                    ON CONFLICT(name) DO UPDATE SET
                        symbol = excluded.symbol,
                        fractional_digits = excluded.fractional_digits,
                        min_balance = excluded.min_balance,
                        max_balance = excluded.max_balance,
                        starter_balance = excluded.starter_balance
                    """;
            case MYSQL, MARIADB -> """
                    INSERT INTO currencies (name, symbol, fractional_digits, min_balance, max_balance, starter_balance)
                    VALUES (?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        symbol = VALUES(symbol),
                        fractional_digits = VALUES(fractional_digits),
                        min_balance = VALUES(min_balance),
                        max_balance = VALUES(max_balance),
                        starter_balance = VALUES(starter_balance)
                    """;
        };
    }

    private String currencyTranslationInsertQuery() {
        return switch (dialect) {
            case SQLITE, POSTGRESQL -> """
                    INSERT INTO currency_translations (currency_name, locale, form, display_name)
                    VALUES (?, ?, ?, ?)
                    ON CONFLICT(currency_name, locale, form) DO UPDATE SET
                        display_name = excluded.display_name
                    """;
            case MYSQL, MARIADB -> """
                    INSERT INTO currency_translations (currency_name, locale, form, display_name)
                    VALUES (?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        display_name = VALUES(display_name)
                    """;
        };
    }

    private void bindDisplayNames(final PreparedStatement statement, final String currencyName, final String form,
                                  final Map<Locale, Component> displayNames) throws SQLException {
        for (final var entry : displayNames.entrySet()) {
            bind(statement,
                    currencyName,
                    entry.getKey().toLanguageTag(),
                    form,
                    MINI_MESSAGE.serialize(entry.getValue())
            );
            statement.addBatch();
        }
    }

    private void bind(final PreparedStatement statement, @Nullable final Object... parameters) throws SQLException {
        for (var i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
    }

    private static @Nullable String worldName(@Nullable final World world) {
        return world != null ? world.key().asString() : null;
    }

    private record AccountKey(UUID uuid, @Nullable String world) {
    }

    @FunctionalInterface
    protected interface ThrowingFunction<T, R> {
        @Nullable
        R apply(T t) throws SQLException;

        static <T, R> ThrowingFunction<T, R> unchecked(final ThrowingFunction<T, R> function) {
            return function;
        }
    }
}
