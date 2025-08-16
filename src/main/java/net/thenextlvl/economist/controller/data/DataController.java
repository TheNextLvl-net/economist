package net.thenextlvl.economist.controller.data;

import net.thenextlvl.economist.api.Account;
import net.thenextlvl.economist.api.currency.Currency;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@NullMarked
public interface DataController {
    @Nullable
    Account getAccount(UUID uuid, @Nullable World world);

    @Unmodifiable
    Set<Account> getAccounts(@Nullable World world);

    Account createAccount(UUID uuid, @Nullable World world);

    BigDecimal getTotalBalance(Currency currency, @Nullable World world);

    List<Account> getOrdered(Currency currency, @Nullable World world, int offset, int limit);
    
    Stream<Account> getAccountsUpdatedSince(Instant lastSync) throws SQLException;

    int prune(Duration duration, @Nullable World world);

    boolean deleteAccount(UUID uuid, @Nullable World world);

    boolean save(Account account);
}
