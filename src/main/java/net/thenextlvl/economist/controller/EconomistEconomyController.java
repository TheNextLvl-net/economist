package net.thenextlvl.economist.controller;

import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.Account;
import net.thenextlvl.economist.api.EconomyController;
import net.thenextlvl.economist.model.EconomistAccount;
import org.bukkit.World;

import java.text.DecimalFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EconomistEconomyController implements EconomyController {
    private final EconomistPlugin plugin;

    public EconomistEconomyController(EconomistPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String format(Number amount, Locale locale) {
        var format = new DecimalFormat();
        format.setCurrency(Currency.getInstance(locale));
        format.setMaximumFractionDigits(plugin.config().fractionalDigits());
        format.setMinimumFractionDigits(plugin.config().fractionalDigits());
        return format.format(amount);
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
        return Optional.of(new EconomistAccount(uuid, null));
    }

    @Override
    public Optional<Account> getAccount(UUID uuid, World world) {
        return Optional.of(new EconomistAccount(uuid, world));
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
        return null;
    }

    @Override
    public CompletableFuture<Optional<Account>> loadAccount(UUID uuid, World world) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteAccount(UUID uuid) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteAccount(UUID uuid, World world) {
        return null;
    }

    @Override
    public int fractionalDigits() {
        return plugin.config().fractionalDigits();
    }
}
