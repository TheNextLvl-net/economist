package net.thenextlvl.economist.controller;

import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.Account;
import net.thenextlvl.economist.api.EconomyController;
import net.thenextlvl.economist.api.currency.Currency;
import net.thenextlvl.economist.api.currency.CurrencyHolder;
import net.thenextlvl.economist.controller.data.DataController;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@NullMarked
public class EconomistEconomyController implements EconomyController {
    private final Map<Identifier, Account> cache = new HashMap<>(); // fixme: has always been cursed
    private final EconomistPlugin plugin;

    public EconomistEconomyController(EconomistPlugin plugin) {
        this.plugin = plugin;
    }

    private record Identifier(UUID uuid, @Nullable World world) {
    }

    public void save() {
        cache.values().forEach(account -> {
            try {
                dataController().save(account);
            } catch (SQLException e) {
                plugin.getComponentLogger().error("Failed to save account {}", account.getOwner(), e);
            }
        });
        cache.clear();
    }

    public void save(Account account) {
        try {
            dataController().save(account);
            cache.remove(new Identifier(account.getOwner(), account.getWorld().orElse(null)));
        } catch (SQLException e) {
            plugin.getComponentLogger().error("Failed to save account {}", account.getOwner(), e);
        }
    }

    private DataController dataController() {
        return plugin.dataController();
    }

    @Override
    public CurrencyHolder getCurrencyHolder() {
        return plugin.currencyHolder();
    }

    @Override
    public CompletableFuture<@Unmodifiable Set<Account>> loadAccounts(@Nullable World world) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return dataController().getAccounts(world);
            } catch (SQLException e) {
                plugin.getComponentLogger().error("Failed to load accounts", e);
                return Set.of();
            }
        });
    }

    @Override
    public @Unmodifiable Set<Account> getAccounts() {
        return Set.copyOf(cache.values());
    }

    @Override
    public @Unmodifiable Set<Account> getAccounts(@Nullable World world) {
        return world == null ? getAccounts() : cache.values().stream()
                .filter(account -> account.getWorld().map(w -> w.equals(world)).orElse(false))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Optional<Account> getAccount(UUID uuid, @Nullable World world) {
        return Optional.ofNullable(cache.get(new Identifier(uuid, world)));
    }

    @Override
    public CompletableFuture<@Unmodifiable List<Account>> tryGetOrdered(Currency currency, @Nullable World world, int start, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var accounts = dataController().getOrdered(currency, world, start, limit); // fixme - use currency
                accounts.forEach(account -> {
                    var identifier = new Identifier(account.getOwner(), world);
                    cache.compute(identifier, (key, value) -> {
                        if (value != null) account.setBalance(value.getBalance(currency), currency);
                        return account;
                    });
                });
                return accounts;
            } catch (SQLException e) {
                plugin.getComponentLogger().error("Failed to load accounts ordered", e);
                return List.of();
            }
        });
    }

    @Override
    public CompletableFuture<Account> createAccount(UUID uuid, @Nullable World world) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var account = dataController().createAccount(uuid, world);
                cache.put(new Identifier(uuid, world), account);
                return account;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to create account", e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<Account>> loadAccount(UUID uuid, @Nullable World world) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var optional = Optional.ofNullable(dataController().getAccount(uuid, world));
                optional.ifPresent(account -> cache.put(new Identifier(uuid, world), account));
                return optional;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load account", e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteAccount(UUID uuid, @Nullable World world) {
        return null; // todo: implement
    }

    @Override
    public CompletableFuture<Boolean> deleteAccounts(List<UUID> accounts, @Nullable World world) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                accounts.forEach(uuid -> cache.remove(new Identifier(uuid, world)));
                return dataController().deleteAccounts(accounts, world);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete accounts", e);
            }
        });
    }
}
