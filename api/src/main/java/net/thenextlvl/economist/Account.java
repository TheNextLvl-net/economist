package net.thenextlvl.economist;

import net.thenextlvl.economist.currency.Currency;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a financial account belonging to a player.
 *
 * @since 0.3.0
 */
@ApiStatus.NonExtendable
public interface Account {
    /**
     * Returns the UUID of the owner of this account.
     *
     * @return the UUID of the owner
     * @since 0.3.0
     */
    UUID getOwner();

    /**
     * Returns the world associated with this account, if any.
     *
     * @return an optional containing the world, or empty for global accounts
     * @since 0.3.0
     */
    Optional<World> getWorld();

    /**
     * Retrieves the balance of the account for the specified currency.
     *
     * @param currency the currency for which the balance is to be retrieved
     * @return the balance of the account
     * @throws IllegalArgumentException if the account cannot hold the specified currency
     * @since 0.3.0
     */
    BigDecimal getBalance(Currency currency);

    /**
     * Deposits the specified amount of the given currency into the account.
     *
     * @param amount   the amount to deposit
     * @param currency the currency to deposit
     * @return the result of the transaction
     * @since 0.3.0
     */
    TransactionResult deposit(Number amount, Currency currency);

    /**
     * Withdraws the specified amount of the given currency from the account.
     *
     * @param amount   the amount to withdraw
     * @param currency the currency to withdraw
     * @return the result of the transaction
     * @since 0.3.0
     */
    TransactionResult withdraw(Number amount, Currency currency);

    /**
     * Sets the balance of the account for the given currency.
     *
     * @param balance  the new balance
     * @param currency the currency of the balance
     * @return the result of the transaction
     * @since 0.3.0
     */
    TransactionResult setBalance(Number balance, Currency currency);

    /**
     * Checks if the account can hold the specified currency.
     *
     * @param currency the currency to check
     * @return {@code true} if the account supports the currency
     * @since 0.3.0
     */
    boolean canHold(Currency currency);
}
