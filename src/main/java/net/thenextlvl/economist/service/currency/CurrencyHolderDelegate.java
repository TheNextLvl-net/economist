package net.thenextlvl.economist.service.currency;

import net.thenextlvl.service.api.economy.currency.Currency;
import net.thenextlvl.service.api.economy.currency.CurrencyHolder;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CurrencyHolderDelegate implements CurrencyHolder {
    private final net.thenextlvl.economist.api.currency.CurrencyHolder delegate;
    private final CurrencyDelegate defaultCurrency;

    public CurrencyHolderDelegate(net.thenextlvl.economist.api.currency.CurrencyHolder delegate) {
        this.defaultCurrency = new CurrencyDelegate(this, delegate.getDefaultCurrency());
        this.delegate = delegate;
    }

    @Override
    public @Unmodifiable Set<Currency> getCurrencies() {
        return delegate.getCurrencies().stream()
                .map(currency -> new CurrencyDelegate(this, currency))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Optional<Currency> getCurrency(String name) {
        return delegate.getCurrency(name).map(currency -> new CurrencyDelegate(this, currency));
    }

    @Override
    public boolean hasCurrency(String name) {
        return delegate.hasCurrency(name);
    }

    @Override
    public Currency createCurrency(String name, Consumer<Currency.Builder> consumer) {
        return new CurrencyDelegate(this, delegate.createCurrency(name, builder ->
                consumer.accept(new CurrencyDelegate.BuilderDelegate(builder))));
    }

    @Override
    public boolean deleteCurrency(String name) {
        return delegate.deleteCurrency(name);
    }

    @Override
    public Currency getDefaultCurrency() {
        return defaultCurrency;
    }

    @Override
    public boolean hasMultiCurrencySupport() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CurrencyHolderDelegate that = (CurrencyHolderDelegate) o;
        return Objects.equals(delegate, that.delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(delegate);
    }
}
