package net.thenextlvl.economist.controller;

import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.bank.Bank;
import net.thenextlvl.economist.api.bank.BankController;
import net.thenextlvl.economist.api.currency.CurrencyHolder;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// todo implement banks
@NullMarked
public class EconomistBankController implements BankController {
    private final EconomistPlugin plugin;

    public EconomistBankController(EconomistPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CurrencyHolder getCurrencyHolder() {
        return plugin.currencyHolder();
    }

    @Override
    public CompletableFuture<Bank> createBank(UUID uuid, String name, @Nullable World world) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Bank>> loadBank(String name) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Bank>> loadBank(UUID uuid, @Nullable World world) {
        return null;
    }

    @Override
    public CompletableFuture<@Unmodifiable Set<Bank>> loadBanks(@Nullable World world) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteBank(String name) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteBank(UUID uuid, @Nullable World world) {
        return null;
    }

    @Override
    public @Unmodifiable Set<Bank> getBanks(@Nullable World world) {
        return Set.of();
    }

    @Override
    public Optional<Bank> getBank(String name) {
        return Optional.empty();
    }

    @Override
    public Optional<Bank> getBank(UUID uuid, @Nullable World world) {
        return Optional.empty();
    }

    @Override
    public boolean hasBank(UUID uuid, @Nullable World world) {
        return false;
    }

    private final Set<Bank> dirtyBanks = new HashSet<>();

    public boolean markDirty(Bank bank) {
        return dirtyBanks.add(bank);
    }

    public void saveDirty() {
        // todo: save dirty banks
        //  dirtyBanks.removeIf(this::save);
    }
}
