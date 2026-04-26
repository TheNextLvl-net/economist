package net.thenextlvl.economist.model;

import net.thenextlvl.economist.api.Account;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@NullMarked
public class EconomistAccount implements Account {
    private BigDecimal balance;
    private final @Nullable World world;
    private final UUID owner;

    public EconomistAccount(final BigDecimal balance, @Nullable final World world, final UUID owner) {
        this.balance = balance;
        this.world = world;
        this.owner = owner;
    }

    @Override
    public Optional<World> getWorld() {
        return Optional.ofNullable(world);
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public synchronized BigDecimal setBalance(final BigDecimal balance) {
        return this.balance = balance;
    }

    @Override
    public synchronized BigDecimal getBalance() {
        return balance;
    }
}
