package net.thenextlvl.economist.api;

import net.thenextlvl.economist.api.currency.Currency;
import net.thenextlvl.economist.api.currency.CurrencyHolder;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * EconomyController is an interface that provides methods for managing and interacting
 * with economic systems, such as currency formatting, account retrieval, and multi-currency support.
 */
@NullMarked
@ApiStatus.NonExtendable
public interface EconomyController {
    /**
     * Retrieves the {@code CurrencyHolder} associated with the economy controller.
     *
     * @return the {@code CurrencyHolder} instance that manages the defined currencies for the controller
     */
    @Contract(pure = true)
    CurrencyHolder getCurrencyHolder();
    
    /**
     * Loads all accounts.
     *
     * @return a {@link CompletableFuture} that completes with an unmodifiable {@link Set} of {@link Account} objects
     * representing all available accounts
     */
    default CompletableFuture<@Unmodifiable Set<Account>> loadAccounts() {
        return loadAccounts(null);
    }

    /**
     * Loads all accounts associated with the specified world.
     *
     * @param world the world for which the accounts are to be loaded
     * @return a {@link CompletableFuture} that completes with an unmodifiable {@link Set} of {@link Account} objects
     * representing all available accounts
     */
    CompletableFuture<@Unmodifiable Set<Account>> loadAccounts(@Nullable World world);

    /**
     * Retrieves all the accounts that are currently loaded.
     *
     * @return an unmodifiable set of accounts
     */
    default @Unmodifiable Set<Account> getAccounts() {
        return getAccounts(null);
    }

    /**
     * Retrieves all the accounts associated with the specified world that are currently loaded.
     *
     * @param world the world for which the accounts are to be retrieved
     * @return an unmodifiable set of accounts for the given world
     */
    @Unmodifiable
    Set<Account> getAccounts(@Nullable World world);

    /**
     * Retrieve the account for the specified player.
     *
     * @param player the player for whom the account will be retrieved
     * @return an optional containing the account, or empty
     */
    default Optional<Account> getAccount(OfflinePlayer player) {
        return getAccount(player, null);
    }

    /**
     * Retrieve the account for the specified uuid and world.
     *
     * @param player the player for whom the account will be retrieved
     * @param world  the world in which the account is located
     * @return an optional containing the account, or empty
     */
    default Optional<Account> getAccount(OfflinePlayer player, @Nullable World world) {
        return getAccount(player.getUniqueId(), world);
    }

    /**
     * Retrieve the account with the specified uuid.
     *
     * @param uuid the uuid of the account to be retrieved
     * @return an optional containing the account, or empty
     */
    default Optional<Account> getAccount(UUID uuid) {
        return getAccount(uuid, null);
    }

    /**
     * Retrieve the account for the specified uuid and world.
     *
     * @param uuid  the uuid of the account to be retrieved
     * @param world the world in which the account is located
     * @return an optional containing the account, or empty
     */
    Optional<Account> getAccount(UUID uuid, @Nullable World world);

    /**
     * Attempts to retrieve a list of accounts in an ordered fashion based on the specified start index and limit.
     *
     * @param start the index at which to start retrieving accounts
     * @param limit the number of accounts to retrieve
     * @return a CompletableFuture that will complete with an unmodifiable list of accounts
     */
    default CompletableFuture<@Unmodifiable List<Account>> tryGetOrdered(Currency currency, int start, int limit) {
        return tryGetOrdered(currency, null, start, limit);
    }

    /**
     * Attempts to retrieve a list of accounts in the specified world, ordered and based
     * on the provided start index and limit.
     *
     * @param world the world in which the accounts are located
     * @param start the index at which to start retrieving accounts
     * @param limit the number of accounts to retrieve
     * @return a CompletableFuture that will complete with an unmodifiable list of accounts
     */
    CompletableFuture<@Unmodifiable List<Account>> tryGetOrdered(Currency currency, @Nullable World world, int start, int limit);

    /**
     * Retrieve the account for the specified player or try to load it.
     *
     * @param player the player for whom the account will be retrieved
     * @return a CompletableFuture that will complete with the retrieved account
     */
    default CompletableFuture<Optional<Account>> tryGetAccount(OfflinePlayer player) {
        return tryGetAccount(player, null);
    }

    /**
     * Retrieve the account for the specified player and world or try to load it.
     *
     * @param player the player for whom the account will be retrieved
     * @param world  the world in which the account is located
     * @return a CompletableFuture that will complete with the retrieved account
     */
    default CompletableFuture<Optional<Account>> tryGetAccount(OfflinePlayer player, @Nullable World world) {
        return tryGetAccount(player.getUniqueId(), world);
    }

    /**
     * Retrieve the account for the specified uuid or try to load it.
     *
     * @param uuid the uuid of the account to be retrieved
     * @return a CompletableFuture that will complete with the retrieved account
     */
    default CompletableFuture<Optional<Account>> tryGetAccount(UUID uuid) {
        return tryGetAccount(uuid, null);
    }

    /**
     * Retrieve the account for the specified uuid and world or try to load it.
     *
     * @param uuid  the uuid of the account to be retrieved
     * @param world the world in which the account is located
     * @return a CompletableFuture that will complete with the retrieved account
     */
    default CompletableFuture<Optional<Account>> tryGetAccount(UUID uuid, @Nullable World world) {
        return getAccount(uuid, world)
                .map(account -> CompletableFuture.completedFuture(Optional.of(account)))
                .orElseGet(() -> loadAccount(uuid, world));
    }

    /**
     * Creates an account for the specified player.
     * <p>
     * Completes with an {@link IllegalStateException} if a similar account already exists
     *
     * @param player the player for whom the account will be created
     * @return a CompletableFuture that will complete with the created account
     */
    @Contract("_ -> new")
    default CompletableFuture<Account> createAccount(OfflinePlayer player) {
        return createAccount(player, null);
    }

    /**
     * Creates an account for the specified player in the specified world.
     * <p>
     * Completes with an {@link IllegalStateException} if a similar account already exists
     *
     * @param player the player for whom the account will be created
     * @param world  the world in which the player's account will be created
     * @return a CompletableFuture that will complete with the created account
     */
    @Contract("_, _ -> new")
    default CompletableFuture<Account> createAccount(OfflinePlayer player, @Nullable World world) {
        return createAccount(player.getUniqueId(), world);
    }

    /**
     * Creates an account with the given uuid.
     * <p>
     * Completes with an {@link IllegalStateException} if a similar account already exists
     *
     * @param uuid the uuid of the account to be created
     * @return a CompletableFuture that will complete with the created account
     */
    @Contract("_ -> new")
    default CompletableFuture<Account> createAccount(UUID uuid) {
        return createAccount(uuid, null);
    }

    /**
     * Creates an account with the given uuid and world.
     * <p>
     * Completes with an {@link IllegalStateException} if a similar account already exists
     *
     * @param uuid  the uuid of the account to be created
     * @param world the world in which the account will be created
     * @return a CompletableFuture that will complete with the created account
     */
    @Contract("_, _ -> new")
    CompletableFuture<Account> createAccount(UUID uuid, @Nullable World world);

    /**
     * Loads the account for the specified player asynchronously.
     *
     * @param player the player for whom the account will be retrieved
     * @return a CompletableFuture that will complete with the retrieved account
     */
    default CompletableFuture<Optional<Account>> loadAccount(OfflinePlayer player) {
        return loadAccount(player, null);
    }

    /**
     * Loads the account for the specified uuid and world asynchronously.
     *
     * @param player the player for whom the account will be retrieved
     * @param world  the world in which the account is located
     * @return a CompletableFuture that will complete with the retrieved account
     */
    default CompletableFuture<Optional<Account>> loadAccount(OfflinePlayer player, @Nullable World world) {
        return loadAccount(player.getUniqueId(), world);
    }

    /**
     * Loads the account with the specified uuid asynchronously.
     *
     * @param uuid the uuid of the account to be retrieved
     * @return a CompletableFuture that will complete with the retrieved account
     */
    default CompletableFuture<Optional<Account>> loadAccount(UUID uuid) {
        return loadAccount(uuid, null);
    }

    /**
     * Loads the account for the specified uuid and world asynchronously.
     *
     * @param uuid  the uuid of the account to be retrieved
     * @param world the world in which the account is located
     * @return a CompletableFuture that will complete with the retrieved account
     */
    CompletableFuture<Optional<Account>> loadAccount(UUID uuid, @Nullable World world);

    /**
     * Deletes the specified account.
     *
     * @param account the account to be deleted
     * @return a {@code CompletableFuture} completing with a boolean indicating whether the account was deleted
     */
    default CompletableFuture<Boolean> deleteAccount(Account account) {
        return deleteAccount(account.getOwner(), account.getWorld().orElse(null));
    }

    /**
     * Deletes the account of the given player.
     *
     * @param player the player whose account will be deleted
     * @return a {@code CompletableFuture} completing with a boolean indicating whether the account was deleted
     */
    default CompletableFuture<Boolean> deleteAccount(OfflinePlayer player) {
        return deleteAccount(player, null);
    }

    /**
     * Deletes the account of the given player in the specified world.
     *
     * @param player the player whose account will be deleted
     * @param world  the world in which the player's account exists
     * @return a {@code CompletableFuture} completing with a boolean indicating whether the account was deleted
     */
    default CompletableFuture<Boolean> deleteAccount(OfflinePlayer player, @Nullable World world) {
        return deleteAccount(player.getUniqueId(), world);
    }

    /**
     * Deletes the account of the given owner's UUID.
     *
     * @param uuid the uuid of the account to be deleted
     * @return a {@code CompletableFuture} completing with a boolean indicating whether the account was deleted
     */
    default CompletableFuture<Boolean> deleteAccount(UUID uuid) {
        return deleteAccount(uuid, null);
    }

    /**
     * Deletes the account of the given owner's uuid in the specified world.
     *
     * @param uuid  the uuid of the account to be deleted
     * @param world the world in which the account exists
     * @return a {@code CompletableFuture} completing with a boolean indicating whether the account was deleted
     */
    CompletableFuture<Boolean> deleteAccount(UUID uuid, @Nullable World world);
}
