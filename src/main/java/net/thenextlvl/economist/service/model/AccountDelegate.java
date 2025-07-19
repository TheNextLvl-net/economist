package net.thenextlvl.economist.service.model;

import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.service.api.economy.Account;
import net.thenextlvl.service.api.economy.currency.Currency;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@NullMarked
public class AccountDelegate implements Account {
    private final net.thenextlvl.economist.api.Account delegate;

    public AccountDelegate(net.thenextlvl.economist.api.Account delegate) {
        this.delegate = delegate;
    }
 
    @Override
    public BigDecimal getBalance(Currency currency) {
        return delegate.getBalance(delegate(currency));
    }

    @Override
    public Optional<World> getWorld() {
        return delegate.getWorld();
    }

    @Override
    public UUID getOwner() {
        return delegate.getOwner();
    }

    @Override
    public BigDecimal setBalance(Number balance, Currency currency) {
        return delegate.setBalance(balance, delegate(currency));
    }
    
    private net.thenextlvl.economist.api.currency.Currency delegate(Currency currency) {
        var plugin = JavaPlugin.getPlugin(EconomistPlugin.class);
        return plugin.currencyHolder().getCurrency(currency.getName())
               .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown currency '" + currency.getName() + "'"
                ));
    }
}
