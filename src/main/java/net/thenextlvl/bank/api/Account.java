package net.thenextlvl.bank.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.UUID;

public interface Account {

    @NotNull UUID getId();

    @Nullable String getOwner();

    void setOwner(@Nullable String name);


    @NotNull BigDecimal getBalance();

    /**
     * Withdraw the amount from the account and returns the new balance.
     *
     * @param amount the amount which will withdraw
     * @return the new account balance
     */
    @NotNull BigDecimal withdraw(@NotNull BigDecimal amount);

    /**
     * Deposit the amount to the account and returns the new balance.
     *
     * @param amount the amount which will deposit
     * @return the new account balance
     */
    @NotNull BigDecimal deposit(@NotNull BigDecimal amount);

    default boolean checkBalance(@NotNull BigDecimal amount) {
        return getBalance().subtract(amount).signum() >= 0;
    }

    default boolean checkBalance(double amount) {
        return checkBalance(new BigDecimal(amount));
    }

    default boolean checkBalance(float amount) {
        return checkBalance(new BigDecimal(amount));
    }

}
