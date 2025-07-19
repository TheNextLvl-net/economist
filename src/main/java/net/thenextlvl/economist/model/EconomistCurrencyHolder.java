package net.thenextlvl.economist.model;

import com.google.common.base.Preconditions;
import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.currency.Currency;
import net.thenextlvl.economist.api.currency.CurrencyHolder;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@NullMarked
public class EconomistCurrencyHolder implements CurrencyHolder {
    private final Set<Currency> currencies = new HashSet<>();

    private final Currency defaultCurrency;
    private final EconomistPlugin plugin;

    public EconomistCurrencyHolder(EconomistPlugin plugin) {
        this.plugin = plugin;
        this.defaultCurrency = new EconomistCurrency(plugin, new EconomistCurrency.Builder("default")); // todo: proper default
    }

    @Override
    public @Unmodifiable Set<Currency> getCurrencies() {
        return Set.copyOf(currencies);
    }

    @Override
    public Optional<Currency> getCurrency(String name) {
        return currencies.stream()
                .filter(currency -> currency.getName().equals(name))
                .findAny();
    }

    @Override
    public boolean hasCurrency(String name) {
        return getCurrency(name).isPresent();
    }

    @Override
    public Currency createCurrency(String name, Consumer<Currency.Builder> consumer) throws IllegalArgumentException {
        var builder = new EconomistCurrency.Builder(name);
        consumer.accept(builder);
        return createCurrency(builder);
    }

    @Override
    public Currency createCurrency(Currency.Builder builder) {
        Preconditions.checkArgument(!hasCurrency(builder.name()), "Currency with name '%s' already exists", builder.name());
        var currency = new EconomistCurrency(plugin, builder);
        currencies.add(currency);
        return currency;
    }

    @Override
    public boolean deleteCurrency(String name) {
        return currencies.removeIf(currency -> currency.getName().equals(name));
    }

    @Override
    public Currency getDefaultCurrency() {
        return defaultCurrency;
    }
}
