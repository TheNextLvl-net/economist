package net.thenextlvl.economist.model;

import net.thenextlvl.economist.api.Account;
import net.thenextlvl.economist.api.currency.Currency;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

@NullMarked
public class EconomistAccount implements Account {
    protected final Map<Currency, BigDecimal> balances = new WeakHashMap<>();
    protected final @Nullable World world;
    protected UUID owner;

    public EconomistAccount(@Nullable World world, UUID owner) {
        this.world = world;
        this.owner = owner;
    }

    @Override
    public BigDecimal getBalance(Currency currency) {
        return balances.get(currency);
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
    public BigDecimal setBalance(Number balance, Currency currency) {
        var decimal = BigDecimal.valueOf(balance.doubleValue());
        balances.put(currency, decimal);
        return decimal;
    }
}
