package net.thenextlvl.economist.currency;

import net.kyori.adventure.text.Component;
import net.thenextlvl.binder.StaticBinder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Manages currencies for an economy provider.
 *
 * @since 0.3.0
 */
@ApiStatus.NonExtendable
public interface CurrencyController {
    /**
     * Resolves the globally bound currency controller instance.
     * <p>
     * This lookup uses the active {@link StaticBinder} for the current class loader and
     * therefore depends on a provider binding a {@link CurrencyController} implementation.
     *
     * @return the bound currency controller
     * @throws IllegalStateException if no currency controller has been bound
     * @since 0.3.0
     */
    static @CheckReturnValue CurrencyController instance() {
        return StaticBinder.getInstance(CurrencyController.class.getClassLoader()).find(CurrencyController.class);
    }

    /**
     * Retrieves the default currency.
     *
     * @return the default currency
     * @since 0.3.0
     */
    @Contract(pure = true)
    Currency getDefaultCurrency();

    /**
     * Retrieves all currencies managed by this controller, including the default currency.
     *
     * @return a stream of all currencies
     * @since 0.3.0
     */
    Stream<Currency> getCurrencies();

    /**
     * Retrieves a currency by its name.
     *
     * @param name the name of the currency to retrieve
     * @return an optional containing the currency, or empty if not found
     * @since 0.3.0
     */
    Optional<Currency> getCurrency(final String name);

    /**
     * Checks whether a currency with the specified name exists.
     *
     * @param name the name of the currency to check
     * @return {@code true} if a currency with the specified name exists, otherwise {@code false}
     * @since 0.3.0
     */
    boolean currencyExists(String name);

    /**
     * Creates a new currency from the provided data.
     *
     * @param data the currency data
     * @return the newly created currency
     * @throws IllegalArgumentException if a currency with the same name already exists
     * @since 0.3.0
     */
    @Contract("_ -> new")
    Currency createCurrency(CurrencyData data) throws IllegalArgumentException;

    /**
     * Creates a new currency with no localized display names.
     *
     * @param name             the unique name of the currency
     * @param symbol           the currency symbol
     * @param fractionalDigits the number of fractional digits
     * @return the newly created currency
     * @throws IllegalArgumentException if a currency with the same name already exists
     * @since 0.3.0
     */
    @Contract("_, _, _ -> new")
    default Currency createCurrency(final String name, final Component symbol, final int fractionalDigits) throws IllegalArgumentException {
        return createCurrency(CurrencyData.of(name, symbol, fractionalDigits));
    }

    /**
     * Deletes the specified currency.
     *
     * @param currency the currency to delete
     * @return {@code true} if the currency was successfully deleted
     * @since 0.3.0
     */
    default boolean deleteCurrency(final Currency currency) {
        return deleteCurrency(currency.getName());
    }

    /**
     * Sets the specified currency as the default currency.
     *
     * @param currency the currency to set as default
     * @return {@code true} if the default currency was updated
     * @since 0.3.0
     */
    default boolean setDefaultCurrency(final Currency currency) {
        return setDefaultCurrency(currency.getName());
    }

    /**
     * Sets a currency by name as the default currency.
     *
     * @param name the name of the currency to set as default
     * @return {@code true} if the default currency was updated
     * @since 0.3.0
     */
    boolean setDefaultCurrency(String name);

    /**
     * Deletes a currency by name.
     *
     * @param name the name of the currency to delete
     * @return {@code true} if the currency was successfully deleted
     * @since 0.3.0
     */
    boolean deleteCurrency(String name);
}
