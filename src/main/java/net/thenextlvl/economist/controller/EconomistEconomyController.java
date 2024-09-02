package net.thenextlvl.economist.controller;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.Account;
import net.thenextlvl.economist.api.EconomyController;
import net.thenextlvl.economist.controller.data.DataController;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EconomistEconomyController implements EconomyController {
    private final EconomistPlugin plugin;
    private final Cache<Identifier, Account> cache;

    public EconomistEconomyController(EconomistPlugin plugin) {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, java.util.concurrent.TimeUnit.MINUTES)
                .<Identifier, Account>removalListener(notification -> {
                    if (!notification.wasEvicted() || notification.getValue() == null) return;
                    if (dataController().save(notification.getValue())) return;
                    plugin.getComponentLogger().error("Failed to save account {} to database",
                            notification.getValue().getOwner());
                })
                .build();
        this.plugin = plugin;
    }


    private record Identifier(UUID uuid, @Nullable World world) {
    }

    @Override
    public String format(Number amount, Locale locale) {
        var format = NumberFormat.getInstance(locale);
        format.setMaximumFractionDigits(plugin.config().fractionalDigits());
        format.setMinimumFractionDigits(plugin.config().fractionalDigits());
        return format.format(amount);
    }

    private DataController dataController() {
        return plugin.dataController();
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public String getCurrencyNamePlural(Locale locale) {
        var format = plugin.bundle().format(locale, "currency.name.plural");
        return format != null ? format : "currency.name.plural";
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public String getCurrencyNameSingular(Locale locale) {
        var format = plugin.bundle().format(locale, "currency.name.singular");
        return format != null ? format : "currency.name.singular";
    }

    @Override
    public String getCurrencySymbol() {
        return plugin.config().currencySymbol();
    }

    @Override
    public Optional<Account> getAccount(UUID uuid) {
        return Optional.ofNullable(cache.getIfPresent(new Identifier(uuid, null)));
    }

    @Override
    public Optional<Account> getAccount(UUID uuid, World world) {
        return Optional.ofNullable(cache.getIfPresent(new Identifier(uuid, world)));
        // return Optional.of(new EconomistAccount(BigDecimal.ZERO, world, uuid));
    }

    @Override
    public CompletableFuture<Account> createAccount(UUID uuid) throws IllegalStateException {
        return null;
    }

    @Override
    public CompletableFuture<Account> createAccount(UUID uuid, World world) throws IllegalStateException {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Account>> loadAccount(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            var optional = Optional.ofNullable(dataController().getAccount(uuid, null));
            optional.ifPresent(account -> cache.put(new Identifier(uuid, null), account));
            return optional;
        });
    }

    @Override
    public CompletableFuture<Optional<Account>> loadAccount(UUID uuid, World world) {
        return CompletableFuture.supplyAsync(() -> {
            var optional = Optional.ofNullable(dataController().getAccount(uuid, world));
            optional.ifPresent(account -> cache.put(new Identifier(uuid, world), account));
            return optional;
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteAccount(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> dataController().deleteAccount(uuid, null));
    }

    @Override
    public CompletableFuture<Boolean> deleteAccount(UUID uuid, World world) {
        return CompletableFuture.supplyAsync(() -> dataController().deleteAccount(uuid, world));
    }

    @Override
    public int fractionalDigits() {
        return plugin.config().fractionalDigits();
    }
}
