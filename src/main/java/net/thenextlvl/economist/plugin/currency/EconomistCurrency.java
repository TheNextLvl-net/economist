package net.thenextlvl.economist.plugin.currency;

import net.kyori.adventure.text.Component;
import net.thenextlvl.economist.currency.Currency;
import net.thenextlvl.economist.currency.CurrencyData;
import net.thenextlvl.economist.plugin.controller.Abbreviation;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class EconomistCurrency implements Currency {
    private final String name;

    private volatile Component symbol = Component.empty();
    private volatile int fractionalDigits = 2;
    private volatile @Nullable BigDecimal maxBalance = null;
    private volatile @Nullable BigDecimal minBalance = BigDecimal.ZERO;

    private final Map<Locale, Component> plural = new ConcurrentHashMap<>();
    private final Map<Locale, Component> singular = new ConcurrentHashMap<>();

    public EconomistCurrency(final String name) {
        this.name = name;
    }

    void apply(final CurrencyData data) {
        symbol = data.symbol();
        fractionalDigits = data.fractionalDigits();
        singular.clear();
        singular.putAll(data.displayNamesSingular());
        plural.clear();
        plural.putAll(data.displayNamesPlural());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<BigDecimal> getMaxBalance() {
        return Optional.ofNullable(maxBalance);
    }

    @Override
    public boolean setMaxBalance(final @Nullable BigDecimal maxBalance) {
        if (Objects.equals(this.maxBalance, maxBalance)) return false;
        this.maxBalance = maxBalance;
        return true;
    }

    @Override
    public Optional<BigDecimal> getMinBalance() {
        return Optional.ofNullable(minBalance);
    }

    @Override
    public boolean setMinBalance(final @Nullable BigDecimal minBalance) {
        if (Objects.equals(this.minBalance, minBalance)) return false;
        this.minBalance = minBalance;
        return true;
    }

    @Override
    public Component getSymbol() {
        return symbol;
    }

    @Override
    public boolean setSymbol(final Component symbol) {
        if (this.symbol.equals(symbol)) return false;
        this.symbol = symbol;
        return true;
    }

    @Override
    public int getFractionalDigits() {
        return fractionalDigits;
    }

    @Override
    public boolean setFractionalDigits(final int fractionalDigits) {
        if (this.fractionalDigits == fractionalDigits) return false;
        this.fractionalDigits = fractionalDigits;
        return true;
    }

    @Override
    public Optional<Component> getDisplayNameSingular(final Locale locale) {
        return Optional.ofNullable(singular.get(locale));
    }

    @Override
    public boolean setDisplayNameSingular(final Locale locale, @Nullable final Component name) {
        final var current = singular.get(locale);
        if (Objects.equals(current, name)) return false;
        if (name == null) singular.remove(locale);
        else singular.put(locale, name);
        return true;
    }

    @Override
    public Optional<Component> getDisplayNamePlural(final Locale locale) {
        return Optional.ofNullable(plural.get(locale));
    }

    @Override
    public boolean setDisplayNamePlural(final Locale locale, @Nullable final Component name) {
        final var current = plural.get(locale);
        if (Objects.equals(current, name)) return false;
        if (name == null) plural.remove(locale);
        else plural.put(locale, name);
        return true;
    }

    @Override
    public CurrencyData toData() {
        return CurrencyData.of(name, symbol, fractionalDigits, singular, plural);
    }

    @Override
    public Component format(final Number amount, final Locale locale) {
        final var decimal = amount instanceof final BigDecimal bigDecimal
                ? bigDecimal
                : new BigDecimal(amount.toString());
        final var format = NumberFormat.getNumberInstance(locale);
        format.setGroupingUsed(true);
        format.setMinimumFractionDigits(Math.max(0, fractionalDigits));
        format.setMaximumFractionDigits(Math.max(0, fractionalDigits));
        return Component.text(Abbreviation.format(decimal.doubleValue(), format, locale));
    }
}
