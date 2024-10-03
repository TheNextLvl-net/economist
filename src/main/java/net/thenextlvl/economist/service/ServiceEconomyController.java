package net.thenextlvl.economist.service;

import lombok.RequiredArgsConstructor;
import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.controller.EconomistEconomyController;
import net.thenextlvl.economist.service.model.ServiceAccount;
import net.thenextlvl.service.api.economy.Account;
import net.thenextlvl.service.api.economy.EconomyController;
import net.thenextlvl.service.api.economy.bank.BankController;
import org.bukkit.World;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class ServiceEconomyController implements EconomyController {
    private final @Nullable ServiceBankController bankController;
    private final EconomistPlugin plugin;

    public void register() {
        var services = plugin.getServer().getServicesManager();
        services.register(EconomyController.class, this, plugin, ServicePriority.Highest);
        if (bankController == null) return;
        services.register(BankController.class, bankController, plugin, ServicePriority.Highest);
    }

    private EconomistEconomyController economyController() {
        return plugin.economyController();
    }

    @Override
    public String format(Number amount) {
        return economyController().format(amount, Locale.US);
    }

    @Override
    public String getCurrencyNamePlural(Locale locale) {
        return economyController().getCurrencyNamePlural(locale);
    }

    @Override
    public String getCurrencyNameSingular(Locale locale) {
        return economyController().getCurrencyNameSingular(locale);
    }

    @Override
    public String getCurrencySymbol() {
        return economyController().getCurrencySymbol();
    }

    @Override
    public Optional<Account> getAccount(UUID uuid) {
        return economyController().getAccount(uuid).map(ServiceAccount::new);
    }

    @Override
    public Optional<Account> getAccount(UUID uuid, World world) {
        return economyController().getAccount(uuid, world).map(ServiceAccount::new);
    }

    @Override
    public CompletableFuture<Account> createAccount(UUID uuid) throws IllegalStateException {
        return economyController().createAccount(uuid).thenApply(ServiceAccount::new);
    }

    @Override
    public CompletableFuture<Account> createAccount(UUID uuid, World world) throws IllegalStateException {
        return economyController().createAccount(uuid, world).thenApply(ServiceAccount::new);
    }

    @Override
    public CompletableFuture<Optional<Account>> loadAccount(UUID uuid) {
        return economyController().loadAccount(uuid).thenApply(optional -> optional.map(ServiceAccount::new));
    }

    @Override
    public CompletableFuture<Optional<Account>> loadAccount(UUID uuid, World world) {
        return economyController().loadAccount(uuid, world).thenApply(optional -> optional.map(ServiceAccount::new));
    }

    @Override
    public CompletableFuture<Boolean> deleteAccount(UUID uuid) {
        return economyController().deleteAccount(uuid);
    }

    @Override
    public CompletableFuture<Boolean> deleteAccount(UUID uuid, World world) {
        return economyController().deleteAccount(uuid, world);
    }

    @Override
    public int fractionalDigits() {
        return economyController().fractionalDigits();
    }

    @Override
    public String getName() {
        return "Economist";
    }
}
