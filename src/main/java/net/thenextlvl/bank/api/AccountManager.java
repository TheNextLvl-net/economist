package net.thenextlvl.bank.api;

import core.annotation.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface AccountManager {

    /**
     * Creates a new account.
     *
     * @param id    the id of the account
     * @param owner the name of the owner
     * @return a future to provide the new account
     */
    CompletableFuture<Account> createAccount(UUID id, @Nullable String owner);

    /**
     * Creates a new account.
     *
     * @param id    the id of the account
     * @return a future to provide the new account
     * @see AccountManager#createAccount(UUID, String)
     */
    default CompletableFuture<Account> createAccount(UUID id) {
        return createAccount(id, null);
    }

    /**
     * Gets the account by the id or null if no account was found.
     *
     * @param id the id of the account
     * @return a future to provide the account or null.
     */
    CompletableFuture<Account> getAccount(UUID id);

    /**
     * Gets the account by the name of the owner or null if no account was found.
     *
     * @param owner the name of the account owner
     * @return a future to provide the account or null.
     * @see AccountManager#getAccount(UUID)
     */
    CompletableFuture<Account> getAccount(String owner);

    /**
     * Deletes an account by its id.
     *
     * @param id the id of the account to delete
     * @return a completable future
     */
    CompletableFuture<Void> deleteAccount(UUID id);

    /**
     * Deletes an account.
     *
     * @param account the account to delete
     * @return a completable future
     */
    default CompletableFuture<Void> deleteAccount(Account account) {
        return deleteAccount(account.getId());
    }
}
