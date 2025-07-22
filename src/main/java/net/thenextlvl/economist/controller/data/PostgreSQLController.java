package net.thenextlvl.economist.controller.data;

import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.Account;
import net.thenextlvl.economist.api.currency.Currency;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@NullMarked
public class PostgreSQLController extends SQLController {
    // todo: implement
    public PostgreSQLController(Connection connection, EconomistPlugin plugin) throws SQLException {
        super(connection, plugin);
    }

    @Override
    public @Nullable Account getAccount(UUID uuid, @Nullable World world) {
        return null;
    }

    @Override
    public @Unmodifiable Set<Account> getAccounts(@Nullable World world) {
        return Set.of();
    }

    @Override
    public Account createAccount(UUID uuid, @Nullable World world) {
        return null;
    }

    @Override
    public BigDecimal getTotalBalance(Currency currency, @Nullable World world) {
        return null;
    }

    @Override
    public List<Account> getOrdered(Currency currency, @Nullable World world, int offset, int limit) {
        return List.of();
    }

    @Override
    public Stream<Account> getAccountsUpdatedSince(Instant lastSync) {
        return Stream.of();
    }

    @Override
    public int prune(Duration duration, @Nullable World world) {
        return 0;
    }

    @Override
    public boolean deleteAccount(UUID uuid, @Nullable World world) {
        return false;
    }

    @Override
    public boolean save(Account account) {
        return false;
    }

    @Override
    protected void setupDatabase() throws SQLException {
    }
}
