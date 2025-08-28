package net.thenextlvl.economist.service;

import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.service.model.BankDelegate;
import net.thenextlvl.service.api.economy.bank.Bank;
import net.thenextlvl.service.api.economy.bank.BankController;
import net.thenextlvl.service.api.economy.currency.CurrencyHolder;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@NullMarked
public class BankControllerDelegate implements BankController {
    private final EconomyControllerDelegate controller;
    private final EconomistPlugin plugin;

    public BankControllerDelegate(EconomyControllerDelegate controller, EconomistPlugin plugin) {
        this.controller = controller;
        this.plugin = plugin;
    }

    @Override
    public CurrencyHolder getCurrencyHolder() {
        return controller.getCurrencyHolder();
    }

    @Override
    public CompletableFuture<Bank> createBank(UUID uuid, String name, @Nullable World world) throws IllegalStateException {
        return plugin.bankController().createBank(uuid, name, world).thenApply(BankDelegate::new);
    }

    @Override
    public CompletableFuture<Optional<Bank>> loadBank(String name) {
        return plugin.bankController().loadBank(name).thenApply(bank -> bank.map(BankDelegate::new));
    }

    @Override
    public CompletableFuture<Optional<Bank>> loadBank(UUID uuid, @Nullable World world) {
        return plugin.bankController().loadBank(uuid, world).thenApply(bank -> bank.map(BankDelegate::new));
    }

    @Override
    public CompletableFuture<@Unmodifiable Set<Bank>> loadBanks(@Nullable World world) {
        return plugin.bankController().loadBanks(world).thenApply(banks -> banks.stream()
                .map(BankDelegate::new).collect(Collectors.toUnmodifiableSet()));
    }

    @Override
    public CompletableFuture<Boolean> deleteBank(String name) {
        return plugin.bankController().deleteBank(name);
    }

    @Override
    public CompletableFuture<Boolean> deleteBank(UUID uuid, @Nullable World world) {
        return plugin.bankController().deleteBank(uuid, world);
    }

    @Override
    public @Unmodifiable Set<Bank> getBanks(@Nullable World world) {
        return plugin.bankController().getBanks(world).stream()
                .map(BankDelegate::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Optional<Bank> getBank(String name) {
        return plugin.bankController().getBank(name).map(BankDelegate::new);
    }

    @Override
    public Optional<Bank> getBank(UUID uuid, @Nullable World world) {
        return plugin.bankController().getBank(uuid, world).map(BankDelegate::new);
    }

    @Override
    public boolean hasBank(UUID uuid, @Nullable World world) {
        return plugin.bankController().hasBank(uuid, world);
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
        return "Economist Banks";
    }
}
