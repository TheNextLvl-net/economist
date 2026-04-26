package net.thenextlvl.economist.currency;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

/**
 * Represents a read-only view of a currency with support for localization, formatting,
 * and symbolic representation.
 *
 * @since 0.3.0
 */
@ApiStatus.NonExtendable
public interface Currency {
    /**
     * Retrieves the unique identifier of the currency.
     *
     * @return the name of the currency as a string
     * @since 0.3.0
     */
    @Contract(pure = true)
    String getName();

    /**
     * Retrieves the maximum balance allowed for this currency.
     *
     * @return the inclusive upper balance bound
     * @since 0.3.0
     */
    BigDecimal getMaxBalance();

    /**
     * Updates the maximum balance allowed for this currency.
     *
     * @param maxBalance the new inclusive upper balance bound
     * @return {@code true} if the value changed
     * @since 0.3.0
     */
    boolean setMaxBalance(BigDecimal maxBalance);

    /**
     * Retrieves the minimum balance allowed for this currency.
     *
     * @return the inclusive lower balance bound
     * @since 0.3.0
     */
    BigDecimal getMinBalance();

    /**
     * Updates the minimum balance allowed for this currency.
     *
     * @param minBalance the new inclusive lower balance bound
     * @return {@code true} if the value changed
     * @since 0.3.0
     */
    boolean setMinBalance(BigDecimal minBalance);

    /**
     * Retrieves the currency symbol.
     *
     * @return the currency symbol as a component
     * @since 0.3.0
     */
    @Contract(pure = true)
    Component getSymbol();

    /**
     * Updates the currency symbol.
     *
     * @param symbol the new symbol component
     * @return {@code true} if the symbol changed
     * @since 0.3.0
     */
    boolean setSymbol(Component symbol);

    /**
     * Retrieves the number of fractional digits used for formatting currency amounts.
     *
     * @return the number of fractional digits
     * @since 0.3.0
     */
    @Contract(pure = true)
    int getFractionalDigits();

    /**
     * Updates the number of fractional digits used for formatting.
     *
     * @param fractionalDigits the new fractional digit count
     * @return {@code true} if the value changed
     * @since 0.3.0
     */
    boolean setFractionalDigits(int fractionalDigits);

    /**
     * Retrieves the singular display name component of the currency based on the audience's locale.
     * <p>
     * If the audience does not specify a locale, {@link Locale#US} is used.
     *
     * @param audience the audience whose locale is used to determine the singular display name
     * @return an {@code Optional} containing the singular display name, or empty
     * @since 0.3.0
     */
    default Optional<Component> getDisplayNameSingular(final Audience audience) {
        return getDisplayNameSingular(audience.getOrDefault(Identity.LOCALE, Locale.US));
    }

    /**
     * Retrieves the singular display name component of the currency for the specified locale.
     *
     * @param locale the locale for which the singular display name should be retrieved
     * @return an {@code Optional} containing the singular display name, or empty
     * @since 0.3.0
     */
    Optional<Component> getDisplayNameSingular(Locale locale);

    /**
     * Updates the singular display name for the specified locale.
     *
     * @param locale the locale to update
     * @param name   the singular display name, or {@code null} to remove it
     * @return {@code true} if the value changed
     * @since 0.3.0
     */
    boolean setDisplayNameSingular(Locale locale, @Nullable Component name);

    /**
     * Retrieves the plural display name component of the currency based on the audience's locale.
     * <p>
     * If the audience does not specify a locale, {@link Locale#US} is used.
     *
     * @param audience the audience whose locale is used to determine the plural display name
     * @return an {@code Optional} containing the plural display name, or empty
     * @since 0.3.0
     */
    default Optional<Component> getDisplayNamePlural(final Audience audience) {
        return getDisplayNamePlural(audience.getOrDefault(Identity.LOCALE, Locale.US));
    }

    /**
     * Retrieves the plural display name component of the currency for the specified locale.
     *
     * @param locale the locale for which the plural display name should be retrieved
     * @return an {@code Optional} containing the plural display name, or empty
     * @since 0.3.0
     */
    Optional<Component> getDisplayNamePlural(Locale locale);

    /**
     * Updates the plural display name for the specified locale.
     *
     * @param locale the locale to update
     * @param name   the plural display name, or {@code null} to remove it
     * @return {@code true} if the value changed
     * @since 0.3.0
     */
    boolean setDisplayNamePlural(Locale locale, @Nullable Component name);

    /**
     * Creates a {@link CurrencyData} snapshot from this currency's current state.
     *
     * @return a currency data instance reflecting this currency's properties
     * @since 0.3.0
     */
    CurrencyData toData();

    /**
     * Formats the specified amount as a component for the given audience.
     *
     * @param amount   the amount to be formatted
     * @param audience the audience to format the amount for
     * @return the formatted amount as a component
     * @see #format(Number, Locale)
     * @since 0.3.0
     */
    default Component format(final Number amount, final Audience audience) {
        return format(amount, audience.getOrDefault(Identity.LOCALE, Locale.US));
    }

    /**
     * Formats the specified amount as a component for the given locale.
     *
     * @param amount the amount to be formatted
     * @param locale the locale to format the amount in
     * @return the formatted amount as a component
     * @since 0.3.0
     */
    Component format(Number amount, Locale locale);
}
