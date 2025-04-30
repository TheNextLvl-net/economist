package net.thenextlvl.economist.controller;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.Account;
import net.thenextlvl.economist.api.EconomyController;
import net.thenextlvl.economist.controller.data.DataController;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class EconomistEconomyController implements EconomyController {
    private final Map<Identifier, Account> cache = new HashMap<>();
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

    @Override
    public String format(Number amount, Locale locale) {
        if (plugin.config.scientificNumbers) return scientificFormat(amount);
        var format = NumberFormat.getInstance(locale);
        format.setRoundingMode(RoundingMode.DOWN);
        format.setMaximumFractionDigits(plugin.config.currency.maxFractionalDigits);
        format.setMinimumFractionDigits(plugin.config.currency.minFractionalDigits);
        if (!plugin.config.abbreviateBalance) return format.format(amount);
        return Abbreviation.format(amount.doubleValue(), format, locale);
    }

    private String scientificFormat(Number amount) {
        var format = "%." + fractionalDigits() + "e";
        return format.formatted(amount.doubleValue());
    }

    private DataController dataController() {
        return plugin.dataController();
    }

    @Override
    public String getCurrencyNamePlural(Locale locale) {
        var translation = plugin.bundle().component("currency.name.plural", locale);
        return PlainTextComponentSerializer.plainText().serialize(translation);
    }

    @Override
    public String getCurrencyNameSingular(Locale locale) {
        var translation = plugin.bundle().component("currency.name.singular", locale);
        return PlainTextComponentSerializer.plainText().serialize(translation);
    }

    @Override
    public String getCurrencySymbol() {
        return plugin.config.currency.symbol;
    }

    @Override
    public CompletableFuture<@Unmodifiable Set<Account>> loadAccounts() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return dataController().getAccounts(null);
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
    public Optional<Account> getAccount(UUID uuid) {
        return Optional.ofNullable(cache.get(new Identifier(uuid, null)));
    }

    @Override
    public Optional<Account> getAccount(UUID uuid, World world) {
        return Optional.ofNullable(cache.get(new Identifier(uuid, world)));
    }

    @Override
    public CompletableFuture<@Unmodifiable List<Account>> tryGetOrdered(int start, int limit) {
        return ordered(null, start, limit);
    }

    @Override
    public CompletableFuture<@Unmodifiable List<Account>> tryGetOrdered(World world, int start, int limit) {
        return ordered(world, start, limit);
    }

    @Override
    public CompletableFuture<Account> createAccount(UUID uuid) {
        return create(uuid, null);
    }

    @Override
    public CompletableFuture<Account> createAccount(UUID uuid, World world) {
        return create(uuid, world);
    }

    @Override
    public CompletableFuture<Optional<Account>> loadAccount(UUID uuid) {
        return load(uuid, null);
    }

    @Override
    public CompletableFuture<Optional<Account>> loadAccount(UUID uuid, World world) {
        return load(uuid, world);
    }

    @Override
    public CompletableFuture<Boolean> deleteAccounts(List<UUID> accounts) {
        return delete(accounts, null);
    }

    @Override
    public CompletableFuture<Boolean> deleteAccounts(List<UUID> accounts, World world) {
        return delete(accounts, world);
    }

    private CompletableFuture<@Unmodifiable List<Account>> ordered(@Nullable World world, int start, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var accounts = dataController().getOrdered(world, start, limit);
                accounts.forEach(account -> {
                    var identifier = new Identifier(account.getOwner(), world);
                    cache.compute(identifier, (key, value) -> {
                        if (value != null) account.setBalance(value.getBalance());
                        return account;
                    });
                });
                return accounts;
            } catch (SQLException e) {
                plugin.getComponentLogger().error("Failed to load accounts", e);
                return List.of();
            }
        });
    }

    private CompletableFuture<Account> create(UUID uuid, @Nullable World world) {
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

    private CompletableFuture<Optional<Account>> load(UUID uuid, @Nullable World world) {
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

    private CompletableFuture<Boolean> delete(List<UUID> accounts, @Nullable World world) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                accounts.forEach(uuid -> cache.remove(new Identifier(uuid, world)));
                return dataController().deleteAccounts(accounts, world);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete accounts", e);
            }
        });
    }

    @Override
    public int fractionalDigits() {
        return plugin.config.currency.maxFractionalDigits;
    }
}
