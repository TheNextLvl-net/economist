package net.thenextlvl.economist.api;

import net.thenextlvl.economist.api.currency.Currency;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
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
     *
     * @param amount   the amount to be deposited
     * @param currency the currency that is being deposited
     * @return the new balance after the deposit
     */
    default BigDecimal deposit(Number amount, Currency currency) {
        var balance = getBalance(currency).add(BigDecimal.valueOf(amount.doubleValue()));
        setBalance(balance, currency);
        return balance;
    }

    /**
     * Retrieves the balance of the account for the specified currency.
     *
     * @param currency the currency for which the balance is to be retrieved
     * @return the balance of the account for the specified currency
     */
    BigDecimal getBalance(Currency currency);

    /**
     * Withdraws the specified amount of the given currency from the account balance.
     *
     * @param amount   the amount to be withdrawn
     * @param currency the currency in which the withdrawal is to be made
     * @return the new balance after the withdrawal
     */
    default BigDecimal withdraw(Number amount, Currency currency) {
        var balance = getBalance(currency).subtract(BigDecimal.valueOf(amount.doubleValue()));
        setBalance(balance, currency);
        return balance;
    }

    /**
     * Returns an optional containing the world associated with this account.
     *
     * @return an {@code Optional<World>} containing the world associated with this account, or empty
     */
    Optional<World> getWorld();

    /**
     * Returns the UUID of the owner of this account.
     *
     * @return the UUID of the owner
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
     *
     * @param balance  the new balance to be set
     * @param currency the currency of the balance
     * @return the new balance after the operation
     */
    BigDecimal setBalance(Number balance, Currency currency);
}
