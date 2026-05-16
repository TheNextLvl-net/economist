package net.thenextlvl.economist.plugin.controller;

import net.thenextlvl.economist.Account;
import net.thenextlvl.economist.EconomyController;
import net.thenextlvl.economist.currency.Currency;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class EconomistEconomyController implements EconomyController {
    private final Map<Identifier, Account> cache = new ConcurrentHashMap<>();
    private final EconomistPlugin plugin;

    public EconomistEconomyController(final EconomistPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Stream<Account> getAccounts() {
        return cache.values().stream();
    }

    @Override
    public Stream<Account> getAccounts(final World world) {
        final var identifier = worldName(world);
        return cache.entrySet().stream()
                .filter(entry -> Objects.equals(identifier, entry.getKey().world()))
                .map(Map.Entry::getValue);
    }

    @Override
    public Optional<Account> getAccount(final OfflinePlayer player) {
        return Optional.ofNullable(cache.get(new Identifier(player.getUniqueId(), null)));
    }

    @Override
    public Optional<Account> getAccount(final OfflinePlayer player, final World world) {
        return Optional.ofNullable(cache.get(new Identifier(player.getUniqueId(), worldName(world))));
    }

    @Override
    public CompletableFuture<Stream<Account>> loadAccounts() {
        return loadAccounts(null);
    }

    @Override
    public CompletableFuture<Stream<Account>> loadAccounts(@Nullable final World world) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return plugin.dataController().getAccounts(world).stream()
                        .map(this::cache);
            } catch (final SQLException exception) {
                throw new RuntimeException("Failed to load accounts", exception);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<Account>> loadAccount(final OfflinePlayer player) {
        return CompletableFuture.supplyAsync(() -> loadAccountInternal(player.getUniqueId(), null));
    }

    @Override
    public CompletableFuture<Optional<Account>> loadAccount(final OfflinePlayer player, final World world) {
        return CompletableFuture.supplyAsync(() -> loadAccountInternal(player.getUniqueId(), world));
    }

    @Override
    public CompletableFuture<Account> createAccount(final OfflinePlayer player) {
        return CompletableFuture.supplyAsync(() -> createAccountInternal(player.getUniqueId(), null));
    }

    @Override
    public CompletableFuture<Account> createAccount(final OfflinePlayer player, final World world) {
        return CompletableFuture.supplyAsync(() -> createAccountInternal(player.getUniqueId(), world));
    }

    @Override
    public CompletableFuture<Boolean> deleteAccount(final OfflinePlayer player) {
        return deleteAccounts(List.of(player.getUniqueId()));
    }

    @Override
    public CompletableFuture<Boolean> deleteAccount(final OfflinePlayer player, final World world) {
        return deleteAccounts(List.of(player.getUniqueId()), world);
    }

    public CompletableFuture<Boolean> deleteAccounts(final List<UUID> accounts) {
        return CompletableFuture.supplyAsync(() -> deleteAccountsInternal(accounts, null));
    }

    public CompletableFuture<Boolean> deleteAccounts(final List<UUID> accounts, final World world) {
        return CompletableFuture.supplyAsync(() -> deleteAccountsInternal(accounts, world));
    }

    public CompletableFuture<@Unmodifiable List<Account>> resolveOrdered(final int start, final int limit) {
        return resolveOrdered(plugin.currencyController().getDefaultCurrency(), start, limit);
    }

    public CompletableFuture<@Unmodifiable List<Account>> resolveOrdered(final World world, final int start, final int limit) {
        return resolveOrdered(plugin.currencyController().getDefaultCurrency(), world, start, limit);
    }

    public CompletableFuture<@Unmodifiable List<Account>> resolveOrdered(final Currency currency, final int start, final int limit) {
        return CompletableFuture.supplyAsync(() -> resolveOrderedInternal(currency, null, start, limit));
    }

    public CompletableFuture<@Unmodifiable List<Account>> resolveOrdered(final Currency currency, final World world, final int start, final int limit) {
        return CompletableFuture.supplyAsync(() -> resolveOrderedInternal(currency, world, start, limit));
    }

    public void save() {
        cache.values().forEach(this::save);
        cache.clear();
    }

    public void save(final Account account) {
        try {
            plugin.dataController().save(account);
            cache.remove(identifier(account));
        } catch (final SQLException exception) {
            plugin.getComponentLogger().error("Failed to save account {}", account.getOwner(), exception);
        }
    }

    private Optional<Account> loadAccountInternal(final UUID uuid, @Nullable final World world) {
        try {
            return Optional.ofNullable(plugin.dataController().getAccount(uuid, world)).map(this::cache);
        } catch (final SQLException exception) {
            throw new RuntimeException("Failed to load account", exception);
        }
    }

    private Account createAccountInternal(final UUID uuid, @Nullable final World world) {
        final var identifier = new Identifier(uuid, worldName(world));
        if (cache.containsKey(identifier)) {
            throw new IllegalStateException("Account already exists for " + uuid);
        }
        try {
            return cache(plugin.dataController().createAccount(uuid, world));
        } catch (final SQLException exception) {
            throw new RuntimeException("Failed to create account", exception);
        }
    }

    private boolean deleteAccountsInternal(final List<UUID> accounts, @Nullable final World world) {
        try {
            final var identifier = worldName(world);
            accounts.forEach(uuid -> cache.remove(new Identifier(uuid, identifier)));
            return plugin.dataController().deleteAccounts(accounts, world);
        } catch (final SQLException exception) {
            throw new RuntimeException("Failed to delete accounts", exception);
        }
    }

    private @Unmodifiable List<Account> resolveOrderedInternal(final Currency currency, @Nullable final World world,
                                                               final int start, final int limit) {
        try {
            return plugin.dataController().getOrdered(currency, world, start, limit).stream()
                    .map(this::cache)
                    .toList();
        } catch (final SQLException exception) {
            throw new RuntimeException("Failed to load ordered accounts", exception);
        }
    }

    private Account cache(final Account account) {
        return cache.computeIfAbsent(identifier(account), ignored -> account);
    }

    private static Identifier identifier(final Account account) {
        return new Identifier(account.getOwner(), account.getWorld().map(EconomistEconomyController::worldName).orElse(null));
    }

    private static @Nullable String worldName(@Nullable final World world) {
        return world != null ? world.key().asString() : null;
    }

    private record Identifier(UUID uuid, @Nullable String world) {
    }
}
