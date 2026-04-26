package net.thenextlvl.economist.service.model;

import net.thenextlvl.service.api.economy.Account;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@NullMarked
public class ServiceAccount implements Account {
    private final net.thenextlvl.economist.api.Account account;

    public ServiceAccount(final net.thenextlvl.economist.api.Account account) {
        this.account = account;
    }

    @Override
    public BigDecimal deposit(final Number amount) {
        return account.deposit(amount);
    }

    @Override
    public BigDecimal getBalance() {
        return account.getBalance();
    }

    @Override
    public BigDecimal withdraw(final Number amount) {
        return account.withdraw(amount);
    }

    @Override
    public Optional<World> getWorld() {
        return account.getWorld();
    }

    @Override
    public UUID getOwner() {
        return account.getOwner();
    }

    @Override
    public void setBalance(final Number balance) {
        account.setBalance(balance);
    }
}
