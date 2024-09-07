package net.thenextlvl.economist.controller;

import lombok.RequiredArgsConstructor;
import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.Account;
import net.thenextlvl.economist.api.EconomyController;
import net.thenextlvl.economist.controller.data.DataController;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class EconomistEconomyController implements EconomyController {
    private final Map<Identifier, Account> cache = new HashMap<>();
    private final EconomistPlugin plugin;

    private record Identifier(UUID uuid, @Nullable World world) {
    }

    public void save() {
        cache.values().forEach(dataController()::save);
        cache.clear();
    }

    public void save(Account account) {
        dataController().save(account);
        cache.remove(new Identifier(account.getOwner(), account.getWorld().orElse(null)));
    }

    @Override
    public String format(Number amount, Locale locale) {
        if (plugin.config().scientificNumbers()) return scientificFormat(amount);
        var format = NumberFormat.getInstance(locale);
        format.setRoundingMode(RoundingMode.DOWN);
        format.setMaximumFractionDigits(plugin.config().currency().maxFractionalDigits());
        format.setMinimumFractionDigits(plugin.config().currency().minFractionalDigits());
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
        return plugin.config().currency().symbol();
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
    public CompletableFuture<Boolean> deleteAccount(UUID uuid) {
        return delete(uuid, null);
    }

    @Override
    public CompletableFuture<Boolean> deleteAccount(UUID uuid, World world) {
        return delete(uuid, world);
    }

    private CompletableFuture<@Unmodifiable List<Account>> ordered(@Nullable World world, int start, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            var accounts = dataController().getOrdered(world, start, limit);
            accounts.forEach(account -> {
                var identifier = new Identifier(account.getOwner(), world);
                cache.compute(identifier, (key, value) -> {
                    if (value != null) account.setBalance(value.getBalance());
                    return account;
                });
            });
            return accounts;
        });
    }

    private CompletableFuture<Account> create(UUID uuid, @Nullable World world) {
        return CompletableFuture.supplyAsync(() -> {
            var account = dataController().createAccount(uuid, world);
            cache.put(new Identifier(uuid, world), account);
            return account;
        });
    }

    private CompletableFuture<Optional<Account>> load(UUID uuid, @Nullable World world) {
        return CompletableFuture.supplyAsync(() -> {
            var optional = Optional.ofNullable(dataController().getAccount(uuid, world));
            optional.ifPresent(account -> cache.put(new Identifier(uuid, world), account));
            return optional;
        });
    }

    private CompletableFuture<Boolean> delete(UUID uuid, @Nullable World world) {
        return CompletableFuture.supplyAsync(() -> {
            cache.remove(new Identifier(uuid, world));
            return dataController().deleteAccount(uuid, world);
        });
    }

    @Override
    public int fractionalDigits() {
        return plugin.config().currency().maxFractionalDigits();
    }
}
