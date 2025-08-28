package net.thenextlvl.economist.model;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.currency.Currency;
import net.thenextlvl.economist.api.currency.CurrencyHolder;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;

@NullMarked
public class EconomistCurrency implements Currency {
    private final EconomistPlugin plugin;

    private String name;

    private final Map<Locale, Component> displayNamesPlural;
    private final Map<Locale, Component> displayNamesSingular;

    private Component symbol;
    private int fractionalDigits;

    public EconomistCurrency(EconomistPlugin plugin, Currency.Builder builder) {
        this.plugin = plugin;
        this.name = builder.name();
        this.displayNamesPlural = new HashMap<>(builder.displayNamesPlural());
        this.displayNamesSingular = new HashMap<>(builder.displayNamesSingular());
        this.fractionalDigits = builder.fractionalDigits().orElse(2);
        this.symbol = builder.symbol().orElseGet(Component::empty);
    }

    @Override
    public CurrencyHolder getHolder() {
        return plugin.currencyHolder();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<Component> getDisplayNameSingular(Locale locale) {
        return Optional.ofNullable(displayNamesSingular.get(locale));
    }

    @Override
    public Optional<Component> getDisplayNamePlural(Locale locale) {
        return Optional.ofNullable(displayNamesPlural.get(locale));
    }

    @Override
    public Component getSymbol() {
        return symbol;
    }

    @Override
    public Component format(Number amount, Locale locale) {
        if (plugin.config.scientificNumbers) return scientificFormat(amount);
        var format = NumberFormat.getInstance(locale);
        format.setRoundingMode(RoundingMode.DOWN);
        format.setMaximumFractionDigits(plugin.config.currency.maxFractionalDigits);
        format.setMinimumFractionDigits(plugin.config.currency.minFractionalDigits);
        if (!plugin.config.abbreviateBalance) return Component.text(format.format(amount));
        return Component.text(Abbreviation.format(amount.doubleValue(), format, locale));
    }

    private Component scientificFormat(Number amount) {
        var format = "%." + fractionalDigits + "e";
        return Component.text(format.formatted(amount.doubleValue()));
    }

    @Override
    public int getFractionalDigits() {
        return fractionalDigits;
    }

    @Override
    public boolean editCurrency(Consumer<Currency.Builder> consumer) {
        var builder = new Builder(name);
        builder.displayNamesPlural.putAll(displayNamesPlural);
        builder.displayNamesSingular.putAll(displayNamesSingular);
        builder.symbol = symbol;
        builder.fractionalDigits = fractionalDigits;

        consumer.accept(builder);

        Preconditions.checkArgument(name.equals(builder.name()) || !getHolder().hasCurrency(name), "Currency with name '%s' already exists", name);

        this.name = builder.name();
        this.displayNamesPlural.putAll(builder.displayNamesPlural());
        this.displayNamesSingular.putAll(builder.displayNamesSingular());
        this.fractionalDigits = builder.fractionalDigits().orElse(plugin.config.currency.maxFractionalDigits);
        this.symbol = builder.symbol().orElseGet(() -> Component.text(plugin.config.currency.symbol));
        return true;
    }

    @Override
    public Currency.Builder toBuilder() {
        var builder = new Builder(name);
        builder.displayNamesPlural.putAll(displayNamesPlural);
        builder.displayNamesSingular.putAll(displayNamesSingular);
        builder.symbol = symbol;
        builder.fractionalDigits = fractionalDigits;
        return builder;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EconomistCurrency that = (EconomistCurrency) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    public static class Builder implements Currency.Builder {
        private String name;

        private @Nullable Component symbol;
        private @Nullable Integer fractionalDigits;

        private final Map<Locale, Component> displayNamesPlural = new HashMap<>();
        private final Map<Locale, Component> displayNamesSingular = new HashMap<>();

        public Builder(String name) {
            this.name = name;
        }

        @Override
        public Currency.Builder name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public @Unmodifiable Map<Locale, Component> displayNamesSingular() {
            return Map.copyOf(displayNamesSingular);
        }

        @Override
        public Currency.Builder displayNameSingular(Locale locale, @Nullable Component name) {
            if (name == null) displayNamesSingular.remove(locale);
            else displayNamesSingular.put(locale, name);
            return this;
        }

        @Override
        public Optional<Component> displayNameSingular(Locale locale) {
            return Optional.ofNullable(displayNamesSingular.get(locale));
        }

        @Override
        public @Unmodifiable Map<Locale, Component> displayNamesPlural() {
            return Map.copyOf(displayNamesPlural);
        }

        @Override
        public Currency.Builder displayNamePlural(Locale locale, @Nullable Component name) {
            if (name == null) displayNamesPlural.remove(locale);
            else displayNamesPlural.put(locale, name);
            return this;
        }

        @Override
        public Optional<Component> displayNamePlural(Locale locale) {
            return Optional.ofNullable(displayNamesPlural.get(locale));
        }

        @Override
        public Currency.Builder symbol(@Nullable Component symbol) {
            this.symbol = symbol;
            return this;
        }

        @Override
        public Optional<Component> symbol() {
            return Optional.ofNullable(symbol);
        }

        @Override
        public Currency.Builder fractionalDigits(@Nullable Integer fractionalDigits) throws IllegalArgumentException {
            Preconditions.checkArgument(fractionalDigits == null || fractionalDigits >= 0, "fractionalDigits must not be negative");
            this.fractionalDigits = fractionalDigits;
            return this;
        }

        @Override
        public OptionalInt fractionalDigits() {
            return fractionalDigits != null ? OptionalInt.of(fractionalDigits) : OptionalInt.empty();
        }
    }
}
