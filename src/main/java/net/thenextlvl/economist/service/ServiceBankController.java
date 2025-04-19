package net.thenextlvl.economist.service;

import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.controller.EconomistBankController;
import net.thenextlvl.economist.service.model.ServiceBank;
import net.thenextlvl.service.api.economy.bank.Bank;
import net.thenextlvl.service.api.economy.bank.BankController;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@NullMarked
public class ServiceBankController implements BankController {
    private final EconomistPlugin plugin;

    public ServiceBankController(EconomistPlugin plugin) {
        this.plugin = plugin;
    }

    private EconomistBankController bankController() {
        return plugin.bankController();
    }

    @Override
    public CompletableFuture<Bank> createBank(UUID uuid, String name) throws IllegalStateException {
        return bankController().createBank(uuid, name).thenApply(ServiceBank::new);
    }

    @Override
    public CompletableFuture<Bank> createBank(UUID uuid, String name, World world) throws IllegalStateException {
        return bankController().createBank(uuid, name, world).thenApply(ServiceBank::new);
    }

    @Override
    public CompletableFuture<Bank> loadBank(String name) {
        return bankController().loadBank(name).thenApply(ServiceBank::new);
    }

    @Override
    public CompletableFuture<Bank> loadBank(UUID uuid) {
        return bankController().loadBank(uuid).thenApply(ServiceBank::new);
    }

    @Override
    public CompletableFuture<Bank> loadBank(UUID uuid, World world) {
        return bankController().loadBank(uuid, world).thenApply(ServiceBank::new);
    }

    @Override
    public CompletableFuture<@Unmodifiable Set<Bank>> loadBanks() {
        return bankController().loadBanks().thenApply(banks -> banks.stream()
                .map(ServiceBank::new).collect(Collectors.toUnmodifiableSet()));
    }

    @Override
    public CompletableFuture<@Unmodifiable Set<Bank>> loadBanks(World world) {
        return bankController().loadBanks(world).thenApply(banks -> banks.stream()
                .map(ServiceBank::new).collect(Collectors.toUnmodifiableSet()));
    }

    @Override
    public CompletableFuture<Boolean> deleteBank(String name) {
        return bankController().deleteBank(name);
    }

    @Override
    public CompletableFuture<Boolean> deleteBank(UUID uuid) {
        return bankController().deleteBank(uuid);
    }

    @Override
    public CompletableFuture<Boolean> deleteBank(UUID uuid, World world) {
        return bankController().deleteBank(uuid, world);
    }

    @Override
    public @Unmodifiable Set<Bank> getBanks() {
        return bankController().getBanks().stream()
                .map(ServiceBank::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public @Unmodifiable Set<Bank> getBanks(World world) {
        return bankController().getBanks(world).stream()
                .map(ServiceBank::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Optional<Bank> getBank(String name) {
        return bankController().getBank(name).map(ServiceBank::new);
    }

    @Override
    public Optional<Bank> getBank(UUID uuid) {
        return bankController().getBank(uuid).map(ServiceBank::new);
    }

    @Override
    public Optional<Bank> getBank(UUID uuid, World world) {
        return bankController().getBank(uuid, world).map(ServiceBank::new);
    }

    @Override
    public String getName() {
        return "Economist Banks";
    }
}
