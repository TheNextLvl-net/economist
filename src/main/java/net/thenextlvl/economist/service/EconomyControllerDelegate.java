package net.thenextlvl.economist.service;

import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.controller.EconomistEconomyController;
import net.thenextlvl.economist.service.currency.CurrencyHolderDelegate;
import net.thenextlvl.economist.service.model.AccountDelegate;
import net.thenextlvl.service.api.economy.Account;
import net.thenextlvl.service.api.economy.EconomyController;
import net.thenextlvl.service.api.economy.bank.BankController;
import net.thenextlvl.service.api.economy.currency.CurrencyHolder;
import org.bukkit.World;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@NullMarked
public class EconomyControllerDelegate implements EconomyController {
    private final CurrencyHolderDelegate holder;
    private final EconomistPlugin plugin;

    public EconomyControllerDelegate(EconomistPlugin plugin) {
        this.holder = new CurrencyHolderDelegate(plugin.currencyHolder());
        this.plugin = plugin;
    }

    public void register(@Nullable BankControllerDelegate controller) {
        var services = plugin.getServer().getServicesManager();
        services.register(EconomyController.class, this, plugin, ServicePriority.Highest);
        if (controller == null) return;
        services.register(BankController.class, controller, plugin, ServicePriority.Highest);
    }

    private EconomistEconomyController economyController() {
        return plugin.economyController();
    }

    @Override
    public CurrencyHolder getCurrencyHolder() {
        return holder;
    }

    @Override
    public CompletableFuture<@Unmodifiable Set<Account>> loadAccounts(@Nullable World world) {
        return economyController().loadAccounts(world).thenApply(accounts ->
                accounts.stream().map(AccountDelegate::new).collect(Collectors.toUnmodifiableSet()));
    }

    @Override
    public @Unmodifiable Set<Account> getAccounts(@Nullable World world) {
        return economyController().getAccounts(world).stream()
                .map(AccountDelegate::new).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Optional<Account> getAccount(UUID uuid, @Nullable World world) {
        return economyController().getAccount(uuid, world).map(AccountDelegate::new);
    }

    @Override
    public CompletableFuture<Account> createAccount(UUID uuid, @Nullable World world) throws IllegalStateException {
        return economyController().createAccount(uuid, world).thenApply(AccountDelegate::new);
    }

    @Override
    public CompletableFuture<Optional<Account>> loadAccount(UUID uuid, @Nullable World world) {
        return economyController().loadAccount(uuid, world).thenApply(optional -> optional.map(AccountDelegate::new));
    }

    @Override
    public CompletableFuture<Boolean> deleteAccount(UUID uuid, @Nullable World world) {
        return economyController().deleteAccount(uuid, world);
    }

    @Override
    public boolean hasMultiWorldSupport() {
        return true;
    }

    @Override
    public EconomistPlugin getPlugin() {
        return plugin;
    }

    @Override
    public String getName() {
        return "Economist";
    }
}
