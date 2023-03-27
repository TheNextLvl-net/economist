package net.thenextlvl.bank.api;

import core.annotation.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.math.BigDecimal;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface Account {

    /**
     * @return the accounts unique id
     */
    UUID getId();

    /**
     * @return the accounts balance
     */
    BigDecimal getBalance();

    /**
     * @return the account owners name
     */
    @Nullable
    String getOwner();


    /**
     * Define the name of the account owner.
     *
     * @param name the account owners name
     */
    void setOwner(@Nullable String name);

    /**
     * Withdraw the amount from the account and returns the new balance.
     *
     * @param amount the amount which will be withdrawn
     * @return the new account balance
     */
    BigDecimal withdraw(BigDecimal amount);

    /**
     * Deposit the amount to the account and returns the new balance.
     *
     * @param amount the amount which will be deposited
     * @return the new account balance
     */
    BigDecimal deposit(BigDecimal amount);

    /**
     * Checks the balance of the account.
     *
     * @param amount the amount to check
     * @return whether the balance is bigger than or equal to the amount to check
     */
    default boolean checkBalance(BigDecimal amount) {
        return getBalance().subtract(amount).signum() >= 0;
    }

    /**
     * @see Account#checkBalance(BigDecimal)
     */
    default boolean checkBalance(double amount) {
        return checkBalance(new BigDecimal(amount));
    }

    /**
     * @see Account#checkBalance(BigDecimal)
     */
    default boolean checkBalance(float amount) {
        return checkBalance(new BigDecimal(amount));
    }
}
