package net.thenextlvl.economist.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.economist.api.Account;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class EconomistAccount implements Account {
    private BigDecimal balance = BigDecimal.ZERO;

    private final UUID owner;
    private final @Nullable World world;

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
    public void setBalance(Number balance) {
        this.balance = new BigDecimal(balance.toString());
    }
}
