package net.thenextlvl.economist.controller.data;

import net.thenextlvl.economist.api.Account;
import net.thenextlvl.economist.api.currency.Currency;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@NullMarked
public interface DataController {
    @Nullable
    Account getAccount(UUID uuid, @Nullable World world) throws SQLException;

    Set<Account> getAccounts(@Nullable World world) throws SQLException;

    Account createAccount(UUID uuid, @Nullable World world) throws SQLException;

    BigDecimal getTotalBalance(Currency currency, @Nullable World world) throws SQLException;

    List<Account> getOrdered(Currency currency, @Nullable World world, int start, int limit) throws SQLException;

    Set<UUID> getAccountOwners(@Nullable World world) throws SQLException;

    boolean deleteAccounts(List<UUID> accounts, @Nullable World world) throws SQLException;

    boolean save(Account account) throws SQLException;
}
