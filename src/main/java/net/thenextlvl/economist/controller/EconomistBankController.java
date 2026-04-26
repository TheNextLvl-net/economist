package net.thenextlvl.economist.controller;

import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.bank.Bank;
import net.thenextlvl.economist.api.bank.BankController;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class EconomistBankController implements BankController {
    private final EconomistPlugin plugin;

    public EconomistBankController(final EconomistPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Bank> createBank(final UUID uuid, final String name) throws IllegalStateException {
        return null;
    }

    @Override
    public CompletableFuture<Bank> createBank(final UUID uuid, final String name, final World world) throws IllegalStateException {
        return null;
    }

    @Override
    public CompletableFuture<Bank> loadBank(final String name) {
        return null;
    }

    @Override
    public CompletableFuture<Bank> loadBank(final UUID uuid) {
        return null;
    }

    @Override
    public CompletableFuture<Bank> loadBank(final UUID uuid, final World world) {
        return null;
    }

    @Override
    public CompletableFuture<@Unmodifiable Set<Bank>> loadBanks() {
        return null;
    }

    @Override
    public CompletableFuture<@Unmodifiable Set<Bank>> loadBanks(final World world) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteBank(final String name) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteBank(final UUID uuid) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteBank(final UUID uuid, final World world) {
        return null;
    }

    @Override
    public @Unmodifiable Set<Bank> getBanks() {
        return Set.of();
    }

    @Override
    public @Unmodifiable Set<Bank> getBanks(final World world) {
        return Set.of();
    }

    @Override
    public Optional<Bank> getBank(final String name) {
        return Optional.empty();
    }

    @Override
    public Optional<Bank> getBank(final UUID uuid) {
        return Optional.empty();
    }

    @Override
    public Optional<Bank> getBank(final UUID uuid, final World world) {
        return Optional.empty();
    }

    @Override
    public boolean hasBank(final String name) {
        return false;
    }

    @Override
    public boolean hasBank(final UUID uuid) {
        return false;
    }

    @Override
    public boolean hasBank(final UUID uuid, final World world) {
        return false;
    }

    public void save() {

    }
}
