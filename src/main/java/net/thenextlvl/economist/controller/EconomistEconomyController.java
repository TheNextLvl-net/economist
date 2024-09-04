package net.thenextlvl.economist.controller;

import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.Account;
import net.thenextlvl.economist.api.EconomyController;
import net.thenextlvl.economist.controller.data.DataController;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class EconomistEconomyController implements EconomyController {
    private final Map<Identifier, Account> cache = new HashMap<>();
    private final EconomistPlugin plugin;

    public EconomistEconomyController(EconomistPlugin plugin) {
        this.plugin = plugin;
    }

    private record Identifier(UUID uuid, @Nullable World world) {
    }

    public void save() {
        cache.values().forEach(this::save);
        cache.clear();
    }

    public void save(Account account) {
        dataController().save(account);
    }

    @Override
    public String format(Number amount, Locale locale) {
        if (plugin.config().scientificNumbers()) return scientificFormat(amount);
        var format = NumberFormat.getInstance(locale);
        format.setRoundingMode(RoundingMode.DOWN);
        format.setMaximumFractionDigits(plugin.config().fractionalDigits());
        format.setMinimumFractionDigits(plugin.config().fractionalDigits());
        if (!plugin.config().abbreviateBalance()) return format.format(amount);
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
        return Optional.ofNullable(cache.get(new Identifier(uuid, null)));
    }

    @Override
    public Optional<Account> getAccount(UUID uuid, World world) {
        return Optional.ofNullable(cache.get(new Identifier(uuid, world)));
    }

    @Override
    public CompletableFuture<Account> createAccount(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            var account = dataController().createAccount(uuid, null);
            cache.put(new Identifier(uuid, null), account);
            return account;
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().error("Failed to create account {}", uuid, throwable);
            return null;
        });
    }

    @Override
    public CompletableFuture<Account> createAccount(UUID uuid, World world) {
        return CompletableFuture.supplyAsync(() -> {
            var account = dataController().createAccount(uuid, world);
            cache.put(new Identifier(uuid, world), account);
            return account;
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().error("Failed to create account {} {}", uuid, world.key().asString(), throwable);
            return null;
        });
    }

    @Override
    public CompletableFuture<Optional<Account>> loadAccount(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            var optional = Optional.ofNullable(dataController().getAccount(uuid, null));
            optional.ifPresent(account -> cache.put(new Identifier(uuid, null), account));
            return optional;
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().error("Failed to load account {}", uuid, throwable);
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Optional<Account>> loadAccount(UUID uuid, World world) {
        return CompletableFuture.supplyAsync(() -> {
            var optional = Optional.ofNullable(dataController().getAccount(uuid, world));
            optional.ifPresent(account -> cache.put(new Identifier(uuid, world), account));
            return optional;
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().error("Failed to load account {} {}", uuid, world.key().asString(), throwable);
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteAccount(UUID uuid) {
        return CompletableFuture.supplyAsync(() ->
                dataController().deleteAccount(uuid, null)
        ).exceptionally(throwable -> {
            plugin.getComponentLogger().error("Failed to delete account {}", uuid, throwable);
            return false;
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteAccount(UUID uuid, World world) {
        return CompletableFuture.supplyAsync(() ->
                dataController().deleteAccount(uuid, world)
        ).exceptionally(throwable -> {
            plugin.getComponentLogger().error("Failed to delete account {} {}", uuid, world.key().asString(), throwable);
            return false;
        });
    }

    @Override
    public int fractionalDigits() {
        return plugin.config().fractionalDigits();
    }
}
