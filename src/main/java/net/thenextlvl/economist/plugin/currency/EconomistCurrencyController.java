package net.thenextlvl.economist.plugin.currency;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.thenextlvl.economist.currency.Currency;
import net.thenextlvl.economist.currency.CurrencyController;
import net.thenextlvl.economist.currency.CurrencyData;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import org.jspecify.annotations.Nullable;

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
    private final EconomistPlugin plugin;
    private volatile Currency defaultCurrency;

    public EconomistCurrencyController(final EconomistPlugin plugin) {
        this.plugin = plugin;
        final var data = CurrencyData.of("euro", Component.text("€"), 2)
                .displayNameSingular(Locale.US, Component.text("Euro"))
                .displayNamePlural(Locale.US, Component.text("Euro"));
        final var currency = new EconomistCurrency(data.name());
        currency.apply(data);
        currencies.put(currency.getName(), currency);
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
        return Optional.ofNullable(currencies.get(name));
    }

    @Override
    public boolean currencyExists(final String name) {
        return currencies.containsKey(name);
    }

    @Override
    public Currency createCurrency(final CurrencyData data) throws IllegalArgumentException {
        if (currencies.containsKey(data.name())) {
            throw new IllegalArgumentException("Currency already exists: " + data.name());
        }
        final var currency = new EconomistCurrency(data.name());
        currency.apply(data.name(data.name()));
        final var existing = currencies.putIfAbsent(data.name(), currency);
        if (existing != null) {
            throw new IllegalArgumentException("Currency already exists: " + data.name());
        }
        return currency;
    }

    @Override
    public boolean deleteCurrency(final String name) {
        if (defaultCurrency.getName().equalsIgnoreCase(name)) return false;
        return currencies.remove(name) != null;
    }

    @Override
    public boolean setDefaultCurrency(final String name) {
        final var currency = currencies.get(name);
        if (currency == null) return false;
        try {
            if (!plugin.dataController().setDefaultCurrency(currency.getName())) return false;
            defaultCurrency = currency;
            return true;
        } catch (final Exception exception) {
            plugin.getComponentLogger().error("Failed to set default currency {}", name, exception);
            return false;
        }
    }

    public void load(final EconomistPlugin plugin) {
        try {
            final var storedCurrencies = plugin.dataController().getCurrencies();
            storedCurrencies.values().forEach(storedCurrency -> {
                final var existing = currencies.get(storedCurrency.data().name());
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
            final var defaultCurrencyName = plugin.dataController().getDefaultCurrencyName();
            if (defaultCurrencyName != null) {
                final var storedDefault = currencies.get(defaultCurrencyName);
                if (storedDefault != null) {
                    defaultCurrency = storedDefault;
                }
            }
            save(plugin);
        } catch (final Exception exception) {
            plugin.getComponentLogger().error("Failed to load currencies from the database", exception);
        }
    }

    public void save(final EconomistPlugin plugin) {
        try {
            plugin.dataController().setDefaultCurrency(defaultCurrency.getName());
        } catch (final Exception exception) {
            plugin.getComponentLogger().error("Failed to save default currency {}", defaultCurrency.getName(), exception);
        }
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
                              final @Nullable BigDecimal minBalance, final @Nullable BigDecimal maxBalance) {
        currency.apply(data);
        currency.setMinBalance(minBalance);
        currency.setMaxBalance(maxBalance);
    }
}
