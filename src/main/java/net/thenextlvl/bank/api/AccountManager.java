package net.thenextlvl.bank.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AccountManager {

    /**
     * Creates a new account
     *
     * @param id    the id of the account
     * @param owner the name of the owner
     * @return a future to provide the new account
     */
    @NotNull CompletableFuture<@NotNull Account> createAccount(@NotNull UUID id, @Nullable String owner);

    /**
     * Gets the account by the id or null if no account exists
     *
     * @param id the id of the account
     * @return a future to provide the account or null.
     */
    @NotNull
    CompletableFuture<@Nullable Account> getAccount(@NotNull UUID id);

    @NotNull
    CompletableFuture<Void> deleteAccount(@NotNull UUID id);


}
