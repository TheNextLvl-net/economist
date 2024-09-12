package net.thenextlvl.economist.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.thenextlvl.economist.api.Account;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class EconomistAccount implements Account {
    private BigDecimal balance;
    private final @Nullable World world;
    private final UUID owner;

    @Override
    public Optional<World> getWorld() {
        return Optional.ofNullable(world);
    }

    @Override
    public synchronized BigDecimal setBalance(BigDecimal balance) {
        return this.balance = balance;
    }

    @Override
    public synchronized BigDecimal getBalance() {
        return balance;
    }
}
