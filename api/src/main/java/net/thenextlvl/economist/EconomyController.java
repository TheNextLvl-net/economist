package net.thenextlvl.economist;

import net.thenextlvl.binder.StaticBinder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Central controller for managing player accounts and currencies.
 *
 * @since 0.3.0
 */
@ApiStatus.NonExtendable
public interface EconomyController {
    /**
     * Resolves the globally bound economy controller instance.
     * <p>
     * This lookup uses the active {@link StaticBinder} for the current class loader and
     * therefore depends on a provider binding an {@link EconomyController} implementation.
     *
     * @return the bound economy controller
     * @throws IllegalStateException if no economy controller has been bound
     * @since 0.3.0
     */
    static @CheckReturnValue EconomyController instance() {
        return StaticBinder.getInstance(EconomyController.class.getClassLoader()).find(EconomyController.class);
    }

    /**
     * Retrieves all currently cached accounts.
     * <p>
     * This method returns immediately without performing any I/O.
     *
     * @return a stream of cached accounts
     * @since 0.3.0
     */
    Stream<Account> getAccounts();

    /**
     * Retrieves all currently cached accounts from the specified world.
     * <p>
     * This method returns immediately without performing any I/O.
     *
     * @param world the world to get all accounts from
     * @return a stream of cached accounts
     * @since 0.3.0
     */
    Stream<Account> getAccounts(World world);

    /**
     * Retrieves a cached account for the specified player.
     *
     * @param player the player whose account is being retrieved
     * @return an optional containing the account, or empty if not cached
     * @since 0.3.0
     */
    Optional<Account> getAccount(OfflinePlayer player);

    /**
     * Retrieves a cached account for the specified player in the given world.
     *
     * @param player the player whose account is being retrieved
     * @param world  the world scope of the account
     * @return an optional containing the account, or empty if not cached
     * @since 0.3.0
     */
    Optional<Account> getAccount(OfflinePlayer player, World world);

    /**
     * Retrieves a cached account with the specified UUID.
     *
     * @param uuid the UUID of the account owner
     * @return an optional containing the account, or empty if not cached
     * @since 0.3.0
     */
    default Optional<Account> getAccount(final UUID uuid) {
        return getAccount(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Retrieves a cached account with the specified UUID in the given world.
     *
     * @param uuid  the UUID of the account owner
     * @param world the world scope of the account
     * @return an optional containing the account, or empty if not cached
     * @since 0.3.0
     */
    default Optional<Account> getAccount(final UUID uuid, final World world) {
        return getAccount(Bukkit.getOfflinePlayer(uuid), world);
    }

    /**
     * Loads all accounts from the backing store.
     *
     * @return a future that completes with a stream of all accounts
     * @since 0.3.0
     */
    CompletableFuture<Stream<Account>> loadAccounts();

    /**
     * Loads all accounts from the backing store.
     *
     * @param world the world from which to load all accounts
     * @return a future that completes with a stream of all accounts
     * @since 0.3.0
     */
    CompletableFuture<Stream<Account>> loadAccounts(World world);

    /**
     * Retrieves the account for the specified player, loading from the backing store if not cached.
     *
     * @param player the player whose account is being resolved
     * @return a future that completes with the account, or empty if it does not exist
     * @since 0.3.0
     */
    default CompletableFuture<Optional<Account>> resolveAccount(final OfflinePlayer player) {
        return getAccount(player)
                .map(account -> CompletableFuture.completedFuture(Optional.of(account)))
                .orElseGet(() -> loadAccount(player));
    }

    /**
     * Retrieves the account for the specified player in the given world, loading if not cached.
     *
     * @param player the player whose account is being resolved
     * @param world  the world scope of the account
     * @return a future that completes with the account, or empty if it does not exist
     * @since 0.3.0
     */
    default CompletableFuture<Optional<Account>> resolveAccount(final OfflinePlayer player, final World world) {
        return getAccount(player, world)
                .map(account -> CompletableFuture.completedFuture(Optional.of(account)))
                .orElseGet(() -> loadAccount(player, world));
    }

    /**
     * Retrieves the account with the specified UUID, loading from the backing store if not cached.
     *
     * @param uuid the UUID of the account owner
     * @return a future that completes with the account, or empty if it does not exist
     * @since 0.3.0
     */
    default CompletableFuture<Optional<Account>> resolveAccount(final UUID uuid) {
        return resolveAccount(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Retrieves the account with the specified UUID in the given world, loading if not cached.
     *
     * @param uuid  the UUID of the account owner
     * @param world the world scope of the account
     * @return a future that completes with the account, or empty if it does not exist
     * @since 0.3.0
     */
    default CompletableFuture<Optional<Account>> resolveAccount(final UUID uuid, final World world) {
        return resolveAccount(Bukkit.getOfflinePlayer(uuid), world);
    }

    /**
     * Loads the account for the specified player from the backing store.
     *
     * @param player the player whose account is being loaded
     * @return a future that completes with the account, or empty if it does not exist
     * @since 0.3.0
     */
    CompletableFuture<Optional<Account>> loadAccount(OfflinePlayer player);

    /**
     * Loads the account for the specified player in the given world from the backing store.
     *
     * @param player the player whose account is being loaded
     * @param world  the world scope of the account
     * @return a future that completes with the account, or empty if it does not exist
     * @since 0.3.0
     */
    CompletableFuture<Optional<Account>> loadAccount(OfflinePlayer player, World world);

    /**
     * Loads the account with the specified UUID from the backing store.
     *
     * @param uuid the UUID of the account owner
     * @return a future that completes with the account, or empty if it does not exist
     * @since 0.3.0
     */
    default CompletableFuture<Optional<Account>> loadAccount(final UUID uuid) {
        return loadAccount(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Loads the account with the specified UUID in the given world from the backing store.
     *
     * @param uuid  the UUID of the account owner
     * @param world the world scope of the account
     * @return a future that completes with the account, or empty if it does not exists
     * @since 0.3.0
     */
    default CompletableFuture<Optional<Account>> loadAccount(final UUID uuid, final World world) {
        return loadAccount(Bukkit.getOfflinePlayer(uuid), world);
    }

    /**
     * Creates an account for the specified player.
     *
     * @param player the player for whom the account will be created
     * @return a future that completes with the created account
     * @throws IllegalStateException if a similar account already exists
     * @since 0.3.0
     */
    CompletableFuture<Account> createAccount(OfflinePlayer player);

    /**
     * Creates an account for the specified player in the given world.
     *
     * @param player the player for whom the account will be created
     * @param world  the world scope of the account
     * @return a future that completes with the created account
     * @throws IllegalStateException if a similar account already exists
     * @since 0.3.0
     */
    CompletableFuture<Account> createAccount(OfflinePlayer player, World world);

    /**
     * Creates an account with the given UUID.
     *
     * @param uuid the UUID of the account owner
     * @return a future that completes with the created account
     * @throws IllegalStateException if a similar account already exists
     * @since 0.3.0
     */
    default CompletableFuture<Account> createAccount(final UUID uuid) {
        return createAccount(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Creates an account with the given UUID in the given world.
     *
     * @param uuid  the UUID of the account owner
     * @param world the world scope of the account
     * @return a future that completes with the created account
     * @throws IllegalStateException if a similar account already exists
     * @since 0.3.0
     */
    default CompletableFuture<Account> createAccount(final UUID uuid, final World world) {
        return createAccount(Bukkit.getOfflinePlayer(uuid), world);
    }

    /**
     * Deletes the specified account.
     *
     * @param account the account to delete
     * @return a future that completes with {@code true} if the account was deleted
     * @since 0.3.0
     */
    default CompletableFuture<Boolean> deleteAccount(final Account account) {
        return account.getWorld()
                .map(world -> deleteAccount(account.getOwner(), world))
                .orElseGet(() -> deleteAccount(account.getOwner()));
    }

    /**
     * Deletes the account of the specified player.
     *
     * @param player the player whose account will be deleted
     * @return a future that completes with {@code true} if the account was deleted
     * @since 0.3.0
     */
    CompletableFuture<Boolean> deleteAccount(OfflinePlayer player);

    /**
     * Deletes the account of the specified player in the given world.
     *
     * @param player the player whose account will be deleted
     * @param world  the world scope of the account
     * @return a future that completes with {@code true} if the account was deleted
     * @since 0.3.0
     */
    CompletableFuture<Boolean> deleteAccount(OfflinePlayer player, World world);

    /**
     * Deletes the account with the specified UUID.
     *
     * @param uuid the UUID of the account owner
     * @return a future that completes with {@code true} if the account was deleted
     * @since 0.3.0
     */
    default CompletableFuture<Boolean> deleteAccount(final UUID uuid) {
        return deleteAccount(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Deletes the account with the specified UUID in the given world.
     *
     * @param uuid  the UUID of the account owner
     * @param world the world scope of the account
     * @return a future that completes with {@code true} if the account was deleted
     * @since 0.3.0
     */
    default CompletableFuture<Boolean> deleteAccount(final UUID uuid, final World world) {
        return deleteAccount(Bukkit.getOfflinePlayer(uuid), world);
    }
}
