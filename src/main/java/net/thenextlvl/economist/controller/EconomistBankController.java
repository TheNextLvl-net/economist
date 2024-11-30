package net.thenextlvl.economist.controller;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class EconomistBankController implements BankController {
    private final EconomistPlugin plugin;

    @Override
    public CompletableFuture<Bank> createBank(UUID uuid, String name) throws IllegalStateException {
        return null;
    }

    @Override
    public CompletableFuture<Bank> createBank(UUID uuid, String name, World world) throws IllegalStateException {
        return null;
    }

    @Override
    public CompletableFuture<Bank> loadBank(String name) {
        return null;
    }

    @Override
    public CompletableFuture<Bank> loadBank(UUID uuid) {
        return null;
    }

    @Override
    public CompletableFuture<Bank> loadBank(UUID uuid, World world) {
        return null;
    }

    @Override
    public CompletableFuture<@Unmodifiable Set<Bank>> loadBanks() {
        return null;
    }

    @Override
    public CompletableFuture<@Unmodifiable Set<Bank>> loadBanks(World world) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteBank(String name) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteBank(UUID uuid) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteBank(UUID uuid, World world) {
        return null;
    }

    @Override
    public @Unmodifiable Set<Bank> getBanks() {
        return Set.of();
    }

    @Override
    public @Unmodifiable Set<Bank> getBanks(World world) {
        return Set.of();
    }

    @Override
    public Optional<Bank> getBank(String name) {
        return Optional.empty();
    }

    @Override
    public Optional<Bank> getBank(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public Optional<Bank> getBank(UUID uuid, World world) {
        return Optional.empty();
    }

    @Override
    public boolean hasBank(String name) {
        return false;
    }

    @Override
    public boolean hasBank(UUID uuid) {
        return false;
    }

    @Override
    public boolean hasBank(UUID uuid, World world) {
        return false;
    }

    public void save() {

    }
}
