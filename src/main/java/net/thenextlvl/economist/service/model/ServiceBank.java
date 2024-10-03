package net.thenextlvl.economist.service.model;

import lombok.RequiredArgsConstructor;
import net.thenextlvl.service.api.economy.bank.Bank;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
public class ServiceBank implements Bank {
    private final net.thenextlvl.economist.api.bank.Bank bank;

    @Override
    public BigDecimal deposit(Number amount) {
        return bank.deposit(amount);
    }

    @Override
    public BigDecimal getBalance() {
        return bank.getBalance();
    }

    @Override
    public BigDecimal withdraw(Number amount) {
        return bank.withdraw(amount);
    }

    @Override
    public Optional<World> getWorld() {
        return bank.getWorld();
    }

    @Override
    public UUID getOwner() {
        return bank.getOwner();
    }

    @Override
    public void setBalance(Number balance) {
        bank.setBalance(balance);
    }

    @Override
    public @Unmodifiable Set<UUID> getMembers() {
        return bank.getMembers();
    }

    @Override
    public String getName() {
        return bank.getName();
    }

    @Override
    public boolean addMember(UUID uuid) {
        return bank.addMember(uuid);
    }

    @Override
    public boolean isMember(UUID uuid) {
        return bank.isMember(uuid);
    }

    @Override
    public boolean removeMember(UUID uuid) {
        return bank.removeMember(uuid);
    }

    @Override
    public boolean setOwner(UUID uuid) {
        return bank.setOwner(uuid);
    }
}
