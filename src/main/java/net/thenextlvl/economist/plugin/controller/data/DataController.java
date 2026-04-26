package net.thenextlvl.economist.plugin.controller.data;

import net.thenextlvl.economist.Account;
import net.thenextlvl.economist.bank.Bank;
import net.thenextlvl.economist.currency.Currency;
import net.thenextlvl.economist.currency.CurrencyData;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface DataController {
    @Nullable
    Account getAccount(UUID uuid, @Nullable World world) throws SQLException;

    Set<Account> getAccounts(@Nullable World world) throws SQLException;

    Account createAccount(UUID uuid, @Nullable World world) throws SQLException;

    BigDecimal getTotalBalance(@Nullable World world) throws SQLException;

    List<Account> getOrdered(@Nullable World world, int start, int limit) throws SQLException;

    BigDecimal getTotalBalance(Currency currency, @Nullable World world) throws SQLException;

    List<Account> getOrdered(Currency currency, @Nullable World world, int start, int limit) throws SQLException;

    Set<UUID> getAccountOwners(@Nullable World world) throws SQLException;

    boolean deleteAccounts(List<UUID> accounts, @Nullable World world) throws SQLException;

    boolean save(Account account) throws SQLException;

    @Nullable
    Bank getBank(String name) throws SQLException;

    @Nullable
    Bank getBank(UUID owner) throws SQLException;

    Set<Bank> getBanks() throws SQLException;

    Bank createBank(UUID owner, String name) throws SQLException;

    boolean deleteBank(String name) throws SQLException;

    boolean deleteBank(UUID owner) throws SQLException;

    boolean save(Bank bank) throws SQLException;

    @Nullable
    String getDefaultCurrencyName() throws SQLException;

    Map<String, StoredCurrency> getCurrencies() throws SQLException;

    boolean setDefaultCurrency(String name) throws SQLException;

    boolean save(Currency currency) throws SQLException;

    boolean deleteCurrency(String name) throws SQLException;

    record StoredCurrency(CurrencyData data, @Nullable BigDecimal minBalance, @Nullable BigDecimal maxBalance,
                          BigDecimal starterBalance) {
    }
}
