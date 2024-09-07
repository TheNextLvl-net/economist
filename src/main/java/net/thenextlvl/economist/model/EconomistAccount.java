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
    public BigDecimal deposit(Number amount) {
        return this.balance = getBalance().add(new BigDecimal(amount.toString()));
    }

    @Override
    public BigDecimal withdraw(Number amount) {
        return this.balance = getBalance().subtract(new BigDecimal(amount.toString()));
    }

    @Override
    public Optional<World> getWorld() {
        return Optional.ofNullable(world);
    }

    @Override
    public BigDecimal setBalance(BigDecimal balance) {
        return this.balance = balance;
    }

    @Override
    public BigDecimal setBalance(Number balance) {
        return setBalance(new BigDecimal(balance.toString()));
    }
}
