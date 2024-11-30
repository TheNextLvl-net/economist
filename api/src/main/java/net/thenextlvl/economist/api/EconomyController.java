package net.thenextlvl.economist.api;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * The AccountController interface provides methods to create, retrieve and delete accounts.
 */
@NullMarked
public interface EconomyController {
    /**
     * Formats the specified amount as a string.
     *
     * @param amount the number amount to be formatted
     * @param locale the locale to use for formatting
     * @return the formatted amount as a string
     */
    String format(Number amount, Locale locale);

    /**
     * Retrieves the plural form of the currency name based on the provided locale.
     *
     * @param locale the locale for which to retrieve the plural currency name
     * @return the plural form of the currency name as a string
     */
    String getCurrencyNamePlural(Locale locale);

    /**
     * Retrieves the name of the currency associated with the specified locale.
     *
     * @param locale the locale for which to retrieve the currency name
     * @return the name of the currency as a string
     */
    String getCurrencyNameSingular(Locale locale);

    /**
     * Retrieves the currency symbol associated with the economy controller.
     *
     * @return the currency symbol as a string
     */
    String getCurrencySymbol();

    /**
     * Retrieve the account for the specified player.
     *
     * @param player the player for whom the account will be retrieved
     * @return an optional containing the account, or empty
     */
    default Optional<Account> getAccount(OfflinePlayer player) {
        return getAccount(player.getUniqueId());
    }

    /**
     * Retrieve the account for the specified uuid and world.
     *
     * @param player the player for whom the account will be retrieved
     * @param world  the world in which the account is located
     * @return an optional containing the account, or empty
     */
    default Optional<Account> getAccount(OfflinePlayer player, World world) {
        return getAccount(player.getUniqueId(), world);
    }

    /**
     * Retrieve the account with the specified uuid.
     *
     * @param uuid the uuid of the account to be retrieved
     * @return an optional containing the account, or empty
     */
    Optional<Account> getAccount(UUID uuid);

    /**
     * Retrieve the account for the specified uuid and world.
     *
     * @param uuid  the uuid of the account to be retrieved
     * @param world the world in which the account is located
     * @return an optional containing the account, or empty
     */
    Optional<Account> getAccount(UUID uuid, World world);

    /**
     * Attempts to retrieve a list of accounts in an ordered fashion based on the specified start index and limit.
     *
     * @param start the index at which to start retrieving accounts
     * @param limit the number of accounts to retrieve
     * @return a CompletableFuture that will complete with an unmodifiable list of accounts
     */
    CompletableFuture<@Unmodifiable List<Account>> tryGetOrdered(int start, int limit);

    /**
     * Attempts to retrieve a list of accounts in the specified world, ordered and based
     * on the provided start index and limit.
     *
     * @param world the world in which the accounts are located
     * @param start the index at which to start retrieving accounts
     * @param limit the number of accounts to retrieve
     *
     * @return a CompletableFuture that will complete with an unmodifiable list of accounts
     */
    CompletableFuture<@Unmodifiable List<Account>> tryGetOrdered(World world, int start, int limit);

    /**
     * Retrieve the account for the specified player or try to load it.
     *
     * @param player the player for whom the account will be retrieved
     * @return a CompletableFuture that will complete with the retrieved account
     */
    default CompletableFuture<Optional<Account>> tryGetAccount(OfflinePlayer player) {
        return getAccount(player)
                .map(account -> CompletableFuture.completedFuture(Optional.of(account)))
                .orElseGet(() -> loadAccount(player));
    }

    /**
     * Retrieve the account for the specified player and world or try to load it.
     *
     * @param player the player for whom the account will be retrieved
     * @param world  the world in which the account is located
     * @return a CompletableFuture that will complete with the retrieved account
     */
    default CompletableFuture<Optional<Account>> tryGetAccount(OfflinePlayer player, World world) {
        return getAccount(player, world)
                .map(account -> CompletableFuture.completedFuture(Optional.of(account)))
                .orElseGet(() -> loadAccount(player, world));
    }

    /**
     * Retrieve the account for the specified uuid or try to load it.
     *
     * @param uuid the uuid of the account to be retrieved
     * @return a CompletableFuture that will complete with the retrieved account
     */
    default CompletableFuture<Optional<Account>> tryGetAccount(UUID uuid) {
        return getAccount(uuid)
                .map(account -> CompletableFuture.completedFuture(Optional.of(account)))
                .orElseGet(() -> loadAccount(uuid));
    }

    /**
     * Retrieve the account for the specified uuid and world or try to load it.
     *
     * @param uuid  the uuid of the account to be retrieved
     * @param world the world in which the account is located
     * @return a CompletableFuture that will complete with the retrieved account
     */
    default CompletableFuture<Optional<Account>> tryGetAccount(UUID uuid, World world) {
        return getAccount(uuid, world)
                .map(account -> CompletableFuture.completedFuture(Optional.of(account)))
                .orElseGet(() -> loadAccount(uuid, world));
    }

    /**
     * Creates an account for the specified player.
     *
     * @param player the player for whom the account will be created
     * @return a CompletableFuture that will complete with the created account
     */
    default CompletableFuture<Account> createAccount(OfflinePlayer player) {
        return createAccount(player.getUniqueId());
    }

    /**
     * Creates an account for the specified player in the specified world.
     *
     * @param player the player for whom the account will be created
     * @param world  the world in which the player's account will be created
     * @return a CompletableFuture that will complete with the created account
     */
    default CompletableFuture<Account> createAccount(OfflinePlayer player, World world) {
        return createAccount(player.getUniqueId(), world);
    }

    /**
     * Creates an account with the given uuid.
     *
     * @param uuid the uuid of the account to be created
     * @return a CompletableFuture that will complete with the created account
     */
    CompletableFuture<Account> createAccount(UUID uuid);

    /**
     * Creates an account with the given uuid and world.
     *
     * @param uuid  the uuid of the account to be created
     * @param world the world in which the account will be created
     * @return a CompletableFuture that will complete with the created account
     */
    CompletableFuture<Account> createAccount(UUID uuid, World world);

    /**
     * Loads the account for the specified player asynchronously.
     *
     * @param player the player for whom the account will be retrieved
     * @return a CompletableFuture that will complete with the retrieved account
     */
    default CompletableFuture<Optional<Account>> loadAccount(OfflinePlayer player) {
        return loadAccount(player.getUniqueId());
    }

    /**
     * Loads the account for the specified uuid and world asynchronously.
     *
     * @param player the player for whom the account will be retrieved
     * @param world  the world in which the account is located
     * @return a CompletableFuture that will complete with the retrieved account
     */
    default CompletableFuture<Optional<Account>> loadAccount(OfflinePlayer player, World world) {
        return loadAccount(player.getUniqueId(), world);
    }

    /**
     * Loads the account with the specified uuid asynchronously.
     *
     * @param uuid the uuid of the account to be retrieved
     * @return a CompletableFuture that will complete with the retrieved account
     */
    CompletableFuture<Optional<Account>> loadAccount(UUID uuid);

    /**
     * Loads the account for the specified uuid and world asynchronously.
     *
     * @param uuid  the uuid of the account to be retrieved
     * @param world the world in which the account is located
     * @return a CompletableFuture that will complete with the retrieved account
     */
    CompletableFuture<Optional<Account>> loadAccount(UUID uuid, World world);

    /**
     * Deletes the specified account.
     *
     * @param account the account to be deleted
     * @return a CompletableFuture that will complete when the account is deleted
     */
    default CompletableFuture<Boolean> deleteAccount(Account account) {
        return account.getWorld()
                .map(world -> deleteAccount(account.getOwner(), world))
                .orElseGet(() -> deleteAccount(account.getOwner()));
    }

    /**
     * Deletes the account of the specified player.
     *
     * @param player the player whose account will be deleted
     * @return a CompletableFuture that will complete when the account is deleted
     */
    default CompletableFuture<Boolean> deleteAccount(OfflinePlayer player) {
        return deleteAccount(player.getUniqueId());
    }

    /**
     * Deletes the account of the specified player in the specified world.
     *
     * @param player the player whose account will be deleted
     * @param world  the world in which the player's account exists
     * @return a CompletableFuture that will complete when the account is deleted
     */
    default CompletableFuture<Boolean> deleteAccount(OfflinePlayer player, World world) {
        return deleteAccount(player.getUniqueId(), world);
    }

    /**
     * Deletes the account with the specified uuid.
     *
     * @param uuid the uuid of the account to be deleted
     * @return a CompletableFuture that will complete when the account is deleted
     */
    default CompletableFuture<Boolean> deleteAccount(UUID uuid) {
        return deleteAccounts(List.of(uuid));
    }

    /**
     * Deletes the account with the specified uuid in the specified world.
     *
     * @param uuid  the uuid of the account to be deleted
     * @param world the world in which the account exists
     * @return a CompletableFuture that will complete when the account is deleted
     */
    default CompletableFuture<Boolean> deleteAccount(UUID uuid, World world) {
        return deleteAccounts(List.of(uuid), world);
    }

    /**
     * Deletes multiple accounts based on the provided list of UUIDs.
     *
     * @param accounts a list of UUIDs corresponding to the accounts to be deleted
     * @return a CompletableFuture that will complete when the accounts are deleted
     */
    CompletableFuture<Boolean> deleteAccounts(List<UUID> accounts);

    /**
     * Deletes multiple accounts based on the provided list of UUIDs and the specified world.
     *
     * @param accounts a list of UUIDs corresponding to the accounts to be deleted
     * @param world the world in which the accounts are located
     * @return a CompletableFuture that will complete when the accounts are deleted
     */
    CompletableFuture<Boolean> deleteAccounts(List<UUID> accounts, World world);

    /**
     * Retrieves the number of fractional digits used for formatting currency amounts.
     *
     * @return the number of fractional digits used for formatting currency amounts
     */
    int fractionalDigits();
}
