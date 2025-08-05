package net.thenextlvl.economist.api.currency;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Represents an entity capable of handling currencies in an economy system.
 * This interface provides methods for formatting, retrieving, creating,
 * and deleting currencies, as well as determining support for multiple currencies.
 */
@ApiStatus.NonExtendable
public interface CurrencyHolder {
    /**
     * Retrieves all currencies managed by the currency holder,
     * including the {@link #getDefaultCurrency() default currency}.
     *
     * @return an unmodifiable set of currencies
     */
    @Unmodifiable
    Set<Currency> getCurrencies();

    /**
     * Retrieves a currency by its name.
     *
     * @param name the name of the currency to retrieve
     * @return an {@code Optional} containing the currency, or empty
     */
    Optional<Currency> getCurrency(String name);

    /**
     * Checks if a currency with the specified name exists.
     *
     * @param name the name of the currency to check for existence
     * @return {@code true} if the currency exists, otherwise {@code false}
     */
    @Contract(pure = true)
    boolean hasCurrency(String name);

    /**
     * Creates a new currency by configuring a {@link Currency.Builder}.
     *
     * @param name     the name of the new currency
     * @param consumer a consumer to configure the {@link Currency.Builder} for currency creation
     * @return the newly created {@link Currency}
     * @throws IllegalArgumentException if a currency with the same name already exists
     */
    @Contract(value = "_, _ -> new", mutates = "this")
    Currency createCurrency(String name, Consumer<Currency.Builder> consumer) throws IllegalArgumentException;

    /**
     * Creates a new currency using the specified builder.
     * <p>
     * This method enables modifying existing currencies by utilizing a
     * pre-configured builder to create a new currency.
     *
     * @param builder the {@link Currency.Builder} containing the configuration for the currency creation
     * @return the newly created {@link Currency}
     * @throws IllegalArgumentException if a currency with the same name already exists
     */
    @Contract(value = "_ -> new", mutates = "this")
    Currency createCurrency(Currency.Builder builder);

    /**
     * Deletes the specified currency.
     * <p>
     * Always returns false when invoked on {@link #getDefaultCurrency default currency}
     *
     * @param currency the currency to delete
     * @return {@code true} if the currency was successfully deleted, otherwise {@code false}
     */
    @Contract(mutates = "this")
    default boolean deleteCurrency(Currency currency) {
        return deleteCurrency(currency.getName());
    }

    /**
     * Deletes a currency with the specified name.
     * <p>
     * Always returns false when invoked on {@link #getDefaultCurrency default currency}
     *
     * @param name the name of the currency to delete
     * @return {@code true} if the currency was successfully deleted, otherwise {@code false}
     */
    @Contract(mutates = "this")
    boolean deleteCurrency(String name);

    /**
     * Retrieves the default currency for this economy controller.
     *
     * @return the default currency
     */
    Currency getDefaultCurrency();
}
