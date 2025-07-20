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

import java.time.Duration;
import java.time.Instant;
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
    private final EconomistPlugin plugin;

    public EconomistEconomyController(EconomistPlugin plugin) {
        this.plugin = plugin;
        var evictionMinutes = plugin.config.cacheEvictionMinutes;
        var saveMinutes = plugin.config.autoSaveMinutes;
        plugin.getServer().getAsyncScheduler().runAtFixedRate(
                plugin, ignored -> performCacheEviction(),
                evictionMinutes, evictionMinutes, TimeUnit.MINUTES
        );
        plugin.getServer().getAsyncScheduler().runAtFixedRate(
                plugin, ignored -> saveAll(), // todo: save much more often but only dirty accounts
                saveMinutes, saveMinutes, TimeUnit.MINUTES
        );
        // todo: some kind of syncing to pull changes from the db to cache to support multiple db connections at once
    }

    public void saveAll() {
        cache.values().forEach(cacheEntry -> save(cacheEntry.account()));
    }

    public void save(Account account) {
        dataController().save(account);
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
        return CompletableFuture.supplyAsync(() -> dataController().getAccounts(world));
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
        return Optional.ofNullable(cache.get(new Identifier(uuid, world))).map(CacheEntry::account); // todo: new?
    }

    @Override
    public CompletableFuture<@Unmodifiable List<Account>> tryGetOrdered(Currency currency, @Nullable World world, int start, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            var accounts = dataController().getOrdered(currency, world, start, limit);
            accounts.forEach(account -> {
                // fixme: caching is cursed here
                //  if a user moves out of the top list in cache the getOrdered will be wrong
                // todo: add "ordered" accounts cache
                var identifier = new Identifier(account.getOwner(), world);
                cache.compute(identifier, (key, value) -> {
                    return value != null ? value.touch() : new CacheEntry(account);
                });
            });
            return accounts;
        });
    }

    @Override
    public CompletableFuture<Account> createAccount(UUID uuid, @Nullable World world) {
        return CompletableFuture.supplyAsync(() -> {
            var account = dataController().createAccount(uuid, world);
            cache.put(new Identifier(uuid, world), new CacheEntry(account));
            return account;
        });
    }

    @Override
    public CompletableFuture<Optional<Account>> loadAccount(UUID uuid, @Nullable World world) {
        return CompletableFuture.supplyAsync(() -> {
            var optional = Optional.ofNullable(dataController().getAccount(uuid, world));
            optional.ifPresent(account -> cache.put(new Identifier(uuid, world), new CacheEntry(account)));
            return optional;
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteAccount(UUID uuid, @Nullable World world) {
        return CompletableFuture.supplyAsync(() -> {
            var success = dataController().deleteAccount(uuid, world);
            if (success) cache.remove(new Identifier(uuid, world)); // todo: new?
            return success;
        });
    }

    private void performCacheEviction() {
        var inactivityDuration = Duration.ofMinutes(plugin.config.cacheEvictionMinutes);
        var evictionThreshold = Instant.now().minus(inactivityDuration);

        cache.entrySet().removeIf(entry -> {
            var identifier = entry.getKey();
            var cacheEntry = entry.getValue();

            if (cacheEntry.lastAccessed().isAfter(evictionThreshold)) return false;
            if (plugin.getServer().getPlayer(identifier.uuid()) != null) return false;

            return dataController().save(cacheEntry.account());
        });
    }

    private record CacheEntry(Account account, Instant lastAccessed) {
        public CacheEntry(Account account) {
            this(account, Instant.now());
        }

        public CacheEntry touch() {
            return new CacheEntry(account, Instant.now());
        }
    }

    private record Identifier(UUID uuid, @Nullable World world) {
    }
}
