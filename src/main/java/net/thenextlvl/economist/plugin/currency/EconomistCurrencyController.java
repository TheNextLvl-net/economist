package net.thenextlvl.economist.plugin.currency;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.thenextlvl.economist.currency.Currency;
import net.thenextlvl.economist.currency.CurrencyController;
import net.thenextlvl.economist.currency.CurrencyData;
import net.thenextlvl.economist.plugin.EconomistPlugin;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class EconomistCurrencyController implements CurrencyController {
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private final Map<String, Currency> currencies = new ConcurrentHashMap<>();
    private final Currency defaultCurrency;

    public EconomistCurrencyController(final EconomistPlugin plugin) {
        final var singularUs = plugin.bundle().component("currency.name.singular", Locale.US);
        final var pluralUs = plugin.bundle().component("currency.name.plural", Locale.US);
        final var data = CurrencyData.of(
                        normalizeName(PLAIN.serialize(singularUs)),
                        Component.text(plugin.config.currency.symbol),
                        plugin.config.currency.maxFractionalDigits
                ).displayNameSingular(Locale.US, singularUs)
                .displayNamePlural(Locale.US, pluralUs)
                .displayNameSingular(Locale.GERMANY, plugin.bundle().component("currency.name.singular", Locale.GERMANY))
                .displayNamePlural(Locale.GERMANY, plugin.bundle().component("currency.name.plural", Locale.GERMANY));
        final var currency = new EconomistCurrency(data.name());
        apply(currency, data);
        currencies.put(key(currency.getName()), currency);
        this.defaultCurrency = currency;
    }

    @Override
    public Currency getDefaultCurrency() {
        return defaultCurrency;
    }

    @Override
    public Stream<Currency> getCurrencies() {
        return currencies.values().stream();
    }

    @Override
    public Optional<Currency> getCurrency(final String name) {
        return Optional.ofNullable(currencies.get(key(name)));
    }

    @Override
    public boolean currencyExists(final String name) {
        return currencies.containsKey(key(name));
    }

    @Override
    public Currency createCurrency(final CurrencyData data) throws IllegalArgumentException {
        final var normalized = normalizeName(data.name());
        final var key = key(normalized);
        if (currencies.containsKey(key)) {
            throw new IllegalArgumentException("Currency already exists: " + data.name());
        }
        final var currency = new EconomistCurrency(normalized);
        apply(currency, data.name(normalized));
        final var existing = currencies.putIfAbsent(key, currency);
        if (existing != null) {
            throw new IllegalArgumentException("Currency already exists: " + data.name());
        }
        return currency;
    }

    @Override
    public boolean deleteCurrency(final String name) {
        if (defaultCurrency.getName().equalsIgnoreCase(name)) return false;
        return currencies.remove(key(name)) != null;
    }

    public void load(final EconomistPlugin plugin) {
        try {
            final var storedCurrencies = plugin.dataController().getCurrencies();
            storedCurrencies.values().forEach(storedCurrency -> {
                final var existing = currencies.get(key(storedCurrency.data().name()));
                if (existing != null) {
                    if (existing instanceof final EconomistCurrency economistCurrency) {
                        apply(economistCurrency, storedCurrency.data(), storedCurrency.minBalance(), storedCurrency.maxBalance());
                    }
                    return;
                }
                final var created = createCurrency(storedCurrency.data());
                created.setMinBalance(storedCurrency.minBalance());
                created.setMaxBalance(storedCurrency.maxBalance());
            });
            save(plugin, defaultCurrency);
        } catch (final Exception exception) {
            plugin.getComponentLogger().error("Failed to load currencies from the database", exception);
        }
    }

    private static void apply(final EconomistCurrency currency, final CurrencyData data) {
        currency.apply(data);
    }

    public void save(final EconomistPlugin plugin) {
        currencies.values().stream()
                .sorted(Comparator.comparing(Currency::getName, String.CASE_INSENSITIVE_ORDER))
                .forEach(currency -> save(plugin, currency));
    }

    public void save(final EconomistPlugin plugin, final Currency currency) {
        try {
            plugin.dataController().save(currency);
        } catch (final Exception exception) {
            plugin.getComponentLogger().error("Failed to save currency {}", currency.getName(), exception);
        }
    }

    public boolean delete(final EconomistPlugin plugin, final String name) {
        if (defaultCurrency.getName().equalsIgnoreCase(name)) return false;
        try {
            plugin.dataController().deleteCurrency(name);
            deleteCurrency(name);
            return true;
        } catch (final Exception exception) {
            plugin.getComponentLogger().error("Failed to delete currency {}", name, exception);
            return false;
        }
    }

    private static void apply(final EconomistCurrency currency, final CurrencyData data,
                              final BigDecimal minBalance, final BigDecimal maxBalance) {
        apply(currency, data);
        currency.setMinBalance(minBalance);
        currency.setMaxBalance(maxBalance);
    }

    private static String key(final String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    private static String normalizeName(final String name) {
        return name.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
    }
}
