package net.thenextlvl.economist.api;

import net.thenextlvl.economist.api.currency.Currency;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Account is an interface representing a financial account.
 */
@NullMarked
@ApiStatus.NonExtendable
public interface Account {
    /**
     * Deposits the specified amount of the given currency into the account balance.
     * <p>
     * Returns {@link BigDecimal#ZERO} if {@link #canHold(Currency)} returns {@code false}
     *
     * @param amount   the amount to be deposited
     * @param currency the currency that is being deposited
     * @return the new balance after the deposit
     */
    default BigDecimal deposit(Number amount, Currency currency) {
        return setBalance(getBalance(currency).add(BigDecimal.valueOf(amount.doubleValue())), currency);
    }

    /**
     * Retrieves all balances of the account.
     * <p>
     * The map contains only the currencies for which there is a defined balance.
     * Currencies without an explicit balance will not be included in the map.
     * <p>
     * This behavior differs from {@link #getBalance(Currency)},
     * which provides a balance for all currencies by defaulting to a starting balance.
     *
     * @return an unmodifiable map of currencies and account balance
     */
    @Unmodifiable
    Map<Currency, BigDecimal> getBalances();

    /**
     * Retrieves the balance of the account for the specified currency.
     *
     * @param currency the currency for which the balance is to be retrieved
     * @return the balance of the account for the specified currency
     */
    BigDecimal getBalance(Currency currency);

    /**
     * Withdraws the specified amount of the given currency from the account balance.
     * <p>
     * Returns {@link BigDecimal#ZERO} if {@link #canHold(Currency)} returns {@code false}
     *
     * @param amount   the amount to be withdrawn
     * @param currency the currency in which the withdrawal is to be made
     * @return the new balance after the withdrawal
     */
    default BigDecimal withdraw(Number amount, Currency currency) {
        return setBalance(getBalance(currency).subtract(BigDecimal.valueOf(amount.doubleValue())), currency);
    }

    /**
     * Returns the world associated with this account.
     *
     * @return an optional containing the world associated with this account, or empty
     */
    Optional<World> getWorld();

    /**
     * Returns the account owner's uuid.
     *
     * @return the account owner's uuid
     */
    UUID getOwner();

    /**
     * Compares this account with another account based on their balances in the specified currency.
     *
     * @param account  the account to be compared
     * @param currency the currency in which the balances should be compared
     * @return a negative integer, zero, or a positive integer if this account's balance
     * is less than, equal to, or greater than the specified account's balance
     */
    default int compareTo(Account account, Currency currency) {
        return getBalance(currency).compareTo(account.getBalance(currency));
    }

    /**
     * Sets the balance of the account to the specified value in the given currency.
     * <p>
     * Returns {@link BigDecimal#ZERO} if {@link #canHold(Currency)} returns {@code false}
     *
     * @param balance  the new balance to be set
     * @param currency the currency of the balance
     * @return the new balance after the operation
     * @see #canHold(Currency)
     */
    BigDecimal setBalance(Number balance, Currency currency);

    /**
     * Checks if the account can hold the specified currency.
     *
     * @param currency the currency to check support for
     * @return {@code true} if the account can hold the specified currency, otherwise {@code false}
     */
    boolean canHold(Currency currency);

    /**
     * Retrieves the timestamp of the account's last update.
     *
     * @return an {@code Instant} representing the time of the last update
     */
    @ApiStatus.Internal
    Instant getLastUpdate();
}
