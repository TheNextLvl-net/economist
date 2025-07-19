package net.thenextlvl.economist.service.currency;

import net.kyori.adventure.text.Component;
import net.thenextlvl.service.api.economy.currency.Currency;
import net.thenextlvl.service.api.economy.currency.CurrencyHolder;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;

@NullMarked
public class CurrencyDelegate implements Currency {
    private final net.thenextlvl.economist.api.currency.Currency currency;
    private final CurrencyHolderDelegate holder;

    public CurrencyDelegate(CurrencyHolderDelegate holder, net.thenextlvl.economist.api.currency.Currency currency) {
        this.currency = currency;
        this.holder = holder;
    }

    @Override
    public CurrencyHolder getHolder() {
        return holder;
    }

    @Override
    public String getName() {
        return currency.getName();
    }

    @Override
    public Optional<Component> getDisplayNameSingular(Locale locale) {
        return currency.getDisplayNameSingular(locale);
    }

    @Override
    public Optional<Component> getDisplayNamePlural(Locale locale) {
        return currency.getDisplayNamePlural(locale);
    }

    @Override
    public Component getSymbol() {
        return currency.getSymbol();
    }

    @Override
    public Component format(Number number, Locale locale) {
        return currency.format(number, locale);
    }

    @Override
    public int getFractionalDigits() {
        return currency.getFractionalDigits();
    }

    @Override
    public boolean editCurrency(Consumer<Builder> consumer) {
        return currency.editCurrency(builder -> consumer.accept(new BuilderDelegate(builder)));
    }

    @Override
    public Builder toBuilder() {
        return new BuilderDelegate(currency.toBuilder());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CurrencyDelegate that = (CurrencyDelegate) o;
        return Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(currency);
    }

    public static class BuilderDelegate implements Currency.Builder {
        private final net.thenextlvl.economist.api.currency.Currency.Builder delegate;

        public BuilderDelegate(net.thenextlvl.economist.api.currency.Currency.Builder delegate) {
            this.delegate = delegate;
        }

        @Override
        public Currency.Builder name(String name) {
            delegate.name(name);
            return this;
        }

        @Override
        public String name() {
            return delegate.name();
        }

        @Override
        public @Unmodifiable Map<Locale, Component> displayNamesSingular() {
            return delegate.displayNamesSingular();
        }

        @Override
        public Currency.Builder displayNameSingular(Locale locale, @Nullable Component name) {
            delegate.displayNameSingular(locale, name);
            return this;
        }

        @Override
        public Optional<Component> displayNameSingular(Locale locale) {
            return delegate.displayNameSingular(locale);
        }

        @Override
        public @Unmodifiable Map<Locale, Component> displayNamesPlural() {
            return delegate.displayNamesPlural();
        }

        @Override
        public Currency.Builder displayNamePlural(Locale locale, @Nullable Component name) {
            delegate.displayNamePlural(locale, name);
            return this;
        }

        @Override
        public Optional<Component> displayNamePlural(Locale locale) {
            return delegate.displayNamePlural(locale);
        }

        @Override
        public Currency.Builder symbol(@Nullable Component symbol) {
            delegate.symbol(symbol);
            return this;
        }

        @Override
        public Optional<Component> symbol() {
            return delegate.symbol();
        }

        @Override
        public Currency.Builder fractionalDigits(@Nullable Integer fractionalDigits) throws IllegalArgumentException {
            delegate.fractionalDigits(fractionalDigits);
            return this;
        }

        @Override
        public OptionalInt fractionalDigits() {
            return delegate.fractionalDigits();
        }
    }
}
