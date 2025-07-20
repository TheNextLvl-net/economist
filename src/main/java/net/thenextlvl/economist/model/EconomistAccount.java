package net.thenextlvl.economist.model;

import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.Account;
import net.thenextlvl.economist.api.currency.Currency;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@NullMarked
public class EconomistAccount implements Account {
    protected final EconomistPlugin plugin;

    protected final @Nullable World world;
    protected final Map<String, BigDecimal> balances;
    protected UUID owner;
    
    // todo: some kind of "dirty" marking to optimize saving?

    public EconomistAccount(EconomistPlugin plugin, Map<String, BigDecimal> balances, @Nullable World world, UUID owner) {
        this.balances = balances;
        this.plugin = plugin;
        this.world = world;
        this.owner = owner;
    }

    //fixme: temp solution
    public Map<String, BigDecimal> getBalances() {
        return balances;
    }

    public EconomistAccount(EconomistPlugin plugin, @Nullable World world, UUID owner) {
        this(plugin, new HashMap<>(), world, owner);
    }

    @Override
    public BigDecimal getBalance(Currency currency) {
        return Objects.requireNonNullElseGet(balances.get(currency.getName()), () ->
                BigDecimal.valueOf(plugin.config.startBalance));
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
        balances.put(currency.getName(), decimal);
        return decimal;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EconomistAccount account = (EconomistAccount) o;
        return Objects.equals(world, account.world) && Objects.equals(owner, account.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, owner);
    }

    @Override
    public String toString() {
        return "EconomistAccount{" +
               "world=" + world +
               ", balances=" + balances +
               ", owner=" + owner +
               '}';
    }
}
