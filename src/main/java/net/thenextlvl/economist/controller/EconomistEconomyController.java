package net.thenextlvl.economist.controller;

import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.Account;
import net.thenextlvl.economist.api.EconomyController;
import net.thenextlvl.economist.api.currency.Currency;
import net.thenextlvl.economist.api.currency.CurrencyHolder;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@NullMarked
public class EconomistEconomyController implements EconomyController {
    private final Map<Identifier, CacheEntry> cache = new ConcurrentHashMap<>();
    private final Set<Account> dirtyAccounts = new HashSet<>();
    private final EconomistPlugin plugin;

    private volatile Instant lastSyncTime = Instant.now();

    public EconomistEconomyController(EconomistPlugin plugin) {
        this.plugin = plugin;
    }

    public void scheduleTasks() {
        var evictionMinutes = plugin.config.cacheEvictionMinutes;
        plugin.getServer().getAsyncScheduler().runAtFixedRate(
                plugin, ignored -> performCacheEviction(),
                evictionMinutes, evictionMinutes, TimeUnit.MINUTES
        );

        var saveSeconds = plugin.config.autoSaveSeconds;
        plugin.getServer().getAsyncScheduler().runAtFixedRate(
                plugin, ignored -> saveDirty(),
                saveSeconds, saveSeconds, TimeUnit.SECONDS
        );

        var syncSeconds = plugin.config.syncIntervalSeconds;
        plugin.getServer().getAsyncScheduler().runAtFixedRate(
                plugin, ignored -> pullChangesFromDatabase(),
                syncSeconds, syncSeconds, TimeUnit.SECONDS
        );
    }

    @Override
    public CurrencyHolder getCurrencyHolder() {
        return plugin.currencyHolder();
    }

    @Override
    public CompletableFuture<@Unmodifiable Set<Account>> loadAccounts(@Nullable World world) {
        return CompletableFuture.supplyAsync(() -> plugin.dataController().getAccounts(world));
    }

    @Override
    public @Unmodifiable Set<Account> getAccounts() {
        return cache.values().stream().map(CacheEntry::account).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public @Unmodifiable Set<Account> getAccounts(@Nullable World world) {
        return world == null ? getAccounts() : cache.values().stream().map(CacheEntry::account)
                .filter(account -> account.getWorld().map(w -> w.equals(world)).orElse(false))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Optional<Account> getAccount(UUID uuid, @Nullable World world) {
        return Optional.ofNullable(cache.get(new Identifier(uuid, world))).map(CacheEntry::account);
    }

    @Override
    public CompletableFuture<@Unmodifiable List<Account>> tryGetOrdered(Currency currency, @Nullable World world, int start, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            var accounts = plugin.dataController().getOrdered(currency, world, start, limit);
            accounts.forEach(account -> {
                var identifier = new Identifier(account);
                cache.compute(identifier, (key, value) -> {
                    if (value != null && value.account().getLastUpdate().isAfter(account.getLastUpdate())) {
                        return new CacheEntry(value.account());
                    } else return new CacheEntry(account);
                });
            });
            return accounts;
        });
    }

    @Override
    public CompletableFuture<Account> createAccount(UUID uuid, @Nullable World world) {
        return CompletableFuture.supplyAsync(() -> {
            var account = plugin.dataController().createAccount(uuid, world);
            cache.put(new Identifier(account), new CacheEntry(account));
            return account;
        });
    }

    @Override
    public CompletableFuture<Optional<Account>> loadAccount(UUID uuid, @Nullable World world) {
        return CompletableFuture.supplyAsync(() -> {
            var optional = Optional.ofNullable(plugin.dataController().getAccount(uuid, world));
            optional.ifPresent(account -> cache.put(new Identifier(account), new CacheEntry(account)));
            return optional;
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteAccount(UUID uuid, @Nullable World world) {
        return CompletableFuture.supplyAsync(() -> {
            var success = plugin.dataController().deleteAccount(uuid, world);
            if (success) cache.remove(new Identifier(uuid, world));
            return success;
        });
    }

    private record CacheEntry(Account account, Instant lastAccessed) {
        public CacheEntry(Account account) {
            this(account, Instant.now());
        }
    }

    private record Identifier(UUID uuid, @Nullable World world) {
        public Identifier(Account account) {
            this(account.getOwner(), account.getWorld().orElse(null));
        }
    }

    private void performCacheEviction() {
        var inactivityDuration = Duration.ofMinutes(plugin.config.cacheEvictionMinutes);
        var evictionThreshold = Instant.now().minus(inactivityDuration);

        cache.entrySet().removeIf(entry -> {
            var identifier = entry.getKey();
            var cacheEntry = entry.getValue();

            if (cacheEntry.lastAccessed().isAfter(evictionThreshold)) return false;
            if (plugin.getServer().getPlayer(identifier.uuid()) != null) return false;

            return plugin.dataController().save(cacheEntry.account());
        });
    }

    private void pullChangesFromDatabase() {
        try {
            var currentSyncTime = Instant.now();
            plugin.dataController().getAccountsUpdatedSince(lastSyncTime).forEach(account -> {
                var identifier = new Identifier(account);
                var existingEntry = cache.get(identifier);

                if (existingEntry == null) {
                    cache.put(identifier, new CacheEntry(account));
                    return;
                }

                var cachedAccount = existingEntry.account();
                if (account.getLastUpdate().isBefore(cachedAccount.getLastUpdate())) return;

                dirtyAccounts.remove(cachedAccount);
                cache.put(identifier, new CacheEntry(account));
            });
            this.lastSyncTime = currentSyncTime;
        } catch (Exception e) {
            plugin.getComponentLogger().error("Failed to pull changes since {} from database", lastSyncTime, e);
        }
    }

    public boolean markDirty(Account account) {
        return dirtyAccounts.add(account);
    }

    public void saveDirty() {
        dirtyAccounts.removeIf(plugin.dataController()::save);
    }
}
