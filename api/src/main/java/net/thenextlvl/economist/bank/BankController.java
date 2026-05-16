package net.thenextlvl.economist.bank;

import net.thenextlvl.economist.currency.CurrencyController;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Controller for managing bank accounts.
 *
 * @since 0.3.0
 */
@ApiStatus.NonExtendable
public interface BankController {
    /**
     * Retrieves the currency controller for managing currencies.
     *
     * @return the currency controller
     * @since 0.3.0
     */
    @Contract(pure = true)
    CurrencyController getCurrencyController();

    /**
     * Retrieves all currently cached banks.
     *
     * @return a stream of cached banks
     * @since 0.3.0
     */
    Stream<Bank> getBanks();

    /**
     * Retrieves all currently cached banks in the specified world.
     *
     * @param world the world to filter by
     * @return a stream of cached banks
     * @since 0.3.0
     */
    Stream<Bank> getBanks(World world);

    /**
     * Retrieves a cached bank with the specified name.
     *
     * @param name the name of the bank
     * @return an optional containing the bank, or empty if not cached
     * @since 0.3.0
     */
    Optional<Bank> getBank(String name);

    /**
     * Retrieves a cached bank for the specified player.
     *
     * @param player the player whose bank is being retrieved
     * @return an optional containing the bank, or empty if not cached
     * @since 0.3.0
     */
    Optional<Bank> getBank(OfflinePlayer player);

    /**
     * Retrieves a cached bank for the specified player in the given world.
     *
     * @param player the player whose bank is being retrieved
     * @param world  the world scope of the bank
     * @return an optional containing the bank, or empty if not cached
     * @since 0.3.0
     */
    Optional<Bank> getBank(OfflinePlayer player, World world);

    /**
     * Retrieves a cached bank owned by the specified player.
     *
     * @param uuid the UUID of the bank owner
     * @return an optional containing the bank, or empty if not cached
     * @since 0.3.0
     */
    default Optional<Bank> getBank(final UUID uuid) {
        return getBank(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Retrieves a cached bank owned by the specified player in the given world.
     *
     * @param uuid  the UUID of the bank owner
     * @param world the world scope of the bank
     * @return an optional containing the bank, or empty if not cached
     * @since 0.3.0
     */
    default Optional<Bank> getBank(final UUID uuid, final World world) {
        return getBank(Bukkit.getOfflinePlayer(uuid), world);
    }

    /**
     * Loads all banks from the backing store.
     *
     * @return a future that completes with a stream of all banks
     * @since 0.3.0
     */
    CompletableFuture<Stream<Bank>> loadBanks();

    /**
     * Loads all banks in the specified world from the backing store.
     *
     * @param world the world from which to load banks
     * @return a future that completes with a stream of banks in the world
     * @since 0.3.0
     */
    CompletableFuture<Stream<Bank>> loadBanks(World world);

    /**
     * Retrieves the bank with the specified name, loading from the backing store if not cached.
     *
     * @param name the name of the bank
     * @return a future that completes with the bank, or empty if it does not exist
     * @since 0.3.0
     */
    default CompletableFuture<Optional<Bank>> resolveBank(final String name) {
        return getBank(name)
                .map(bank -> CompletableFuture.completedFuture(Optional.of(bank)))
                .orElseGet(() -> loadBank(name));
    }

    /**
     * Retrieves the bank for the specified player, loading if not cached.
     *
     * @param player the player whose bank is being resolved
     * @return a future that completes with the bank, or empty if it does not exist
     * @since 0.3.0
     */
    default CompletableFuture<Optional<Bank>> resolveBank(final OfflinePlayer player) {
        return getBank(player)
                .map(bank -> CompletableFuture.completedFuture(Optional.of(bank)))
                .orElseGet(() -> loadBank(player));
    }

    /**
     * Retrieves the bank for the specified player in the given world, loading if not cached.
     *
     * @param player the player whose bank is being resolved
     * @param world  the world scope of the bank
     * @return a future that completes with the bank, or empty if it does not exist
     * @since 0.3.0
     */
    default CompletableFuture<Optional<Bank>> resolveBank(final OfflinePlayer player, final World world) {
        return getBank(player, world)
                .map(bank -> CompletableFuture.completedFuture(Optional.of(bank)))
                .orElseGet(() -> loadBank(player, world));
    }

    /**
     * Retrieves the bank owned by the specified player, loading if not cached.
     *
     * @param uuid the UUID of the bank owner
     * @return a future that completes with the bank, or empty if it does not exist
     * @since 0.3.0
     */
    default CompletableFuture<Optional<Bank>> resolveBank(final UUID uuid) {
        return resolveBank(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Retrieves the bank owned by the specified player in the given world, loading if not cached.
     *
     * @param uuid  the UUID of the bank owner
     * @param world the world scope of the bank
     * @return a future that completes with the bank, or empty if it does not exist
     * @since 0.3.0
     */
    default CompletableFuture<Optional<Bank>> resolveBank(final UUID uuid, final World world) {
        return resolveBank(Bukkit.getOfflinePlayer(uuid), world);
    }

    /**
     * Loads the bank with the specified name from the backing store.
     *
     * @param name the name of the bank
     * @return a future that completes with the bank, or empty if it does not exist
     * @since 0.3.0
     */
    CompletableFuture<Optional<Bank>> loadBank(String name);

    /**
     * Loads the bank for the specified player from the backing store.
     *
     * @param player the player whose bank is being loaded
     * @return a future that completes with the bank, or empty if it does not exist
     * @since 0.3.0
     */
    CompletableFuture<Optional<Bank>> loadBank(OfflinePlayer player);

    /**
     * Loads the bank for the specified player in the given world from the backing store.
     *
     * @param player the player whose bank is being loaded
     * @param world  the world scope of the bank
     * @return a future that completes with the bank, or empty if it does not exist
     * @since 0.3.0
     */
    CompletableFuture<Optional<Bank>> loadBank(OfflinePlayer player, World world);

    /**
     * Loads the bank owned by the specified player from the backing store.
     *
     * @param uuid the UUID of the bank owner
     * @return a future that completes with the bank, or empty if it does not exist
     * @since 0.3.0
     */
    default CompletableFuture<Optional<Bank>> loadBank(final UUID uuid) {
        return loadBank(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Loads the bank owned by the specified player in the given world from the backing store.
     *
     * @param uuid  the UUID of the bank owner
     * @param world the world scope of the bank
     * @return a future that completes with the bank, or empty if it does not exist
     * @since 0.3.0
     */
    default CompletableFuture<Optional<Bank>> loadBank(final UUID uuid, final World world) {
        return loadBank(Bukkit.getOfflinePlayer(uuid), world);
    }

    /**
     * Creates a bank for the specified player with the given name.
     *
     * @param player the owner of the bank
     * @param name   the unique name of the bank
     * @return a future that completes with the created bank
     * @throws IllegalStateException if a bank with that owner or name already exists
     * @since 0.3.0
     */
    @Contract("_, _ -> new")
    CompletableFuture<Bank> createBank(OfflinePlayer player, String name);

    /**
     * Creates a bank for the specified player in the given world.
     *
     * @param player the owner of the bank
     * @param name   the unique name of the bank
     * @param world  the world scope of the bank
     * @return a future that completes with the created bank
     * @throws IllegalStateException if a bank with that owner or name already exists
     * @since 0.3.0
     */
    @Contract("_, _, _ -> new")
    CompletableFuture<Bank> createBank(OfflinePlayer player, String name, World world);

    /**
     * Creates a bank for the specified owner with the given name.
     *
     * @param uuid the UUID of the bank owner
     * @param name the unique name of the bank
     * @return a future that completes with the created bank
     * @throws IllegalStateException if a bank with that owner or name already exists
     * @since 0.3.0
     */
    @Contract("_, _ -> new")
    default CompletableFuture<Bank> createBank(final UUID uuid, final String name) {
        return createBank(Bukkit.getOfflinePlayer(uuid), name);
    }

    /**
     * Creates a bank for the specified owner with the given name in the given world.
     *
     * @param uuid  the UUID of the bank owner
     * @param name  the unique name of the bank
     * @param world the world scope of the bank
     * @return a future that completes with the created bank
     * @throws IllegalStateException if a bank with that owner or name already exists
     * @since 0.3.0
     */
    @Contract("_, _, _ -> new")
    default CompletableFuture<Bank> createBank(final UUID uuid, final String name, final World world) {
        return createBank(Bukkit.getOfflinePlayer(uuid), name, world);
    }

    /**
     * Deletes the specified bank.
     *
     * @param bank the bank to delete
     * @return a future that completes with {@code true} if the bank was deleted
     * @since 0.3.0
     */
    default CompletableFuture<Boolean> deleteBank(final Bank bank) {
        return bank.getWorld()
                .map(world -> deleteBank(bank.getOwner(), world))
                .orElseGet(() -> deleteBank(bank.getOwner()));
    }

    /**
     * Deletes the bank of the specified player.
     *
     * @param player the player whose bank will be deleted
     * @return a future that completes with {@code true} if the bank was deleted
     * @since 0.3.0
     */
    CompletableFuture<Boolean> deleteBank(OfflinePlayer player);

    /**
     * Deletes the bank of the specified player in the given world.
     *
     * @param player the player whose bank will be deleted
     * @param world  the world scope of the bank
     * @return a future that completes with {@code true} if the bank was deleted
     * @since 0.3.0
     */
    CompletableFuture<Boolean> deleteBank(OfflinePlayer player, World world);

    /**
     * Deletes the bank with the specified name.
     *
     * @param name the name of the bank to delete
     * @return a future that completes with {@code true} if the bank was deleted
     * @since 0.3.0
     */
    CompletableFuture<Boolean> deleteBank(String name);

    /**
     * Deletes the bank owned by the specified player.
     *
     * @param uuid the UUID of the bank owner
     * @return a future that completes with {@code true} if the bank was deleted
     * @since 0.3.0
     */
    default CompletableFuture<Boolean> deleteBank(final UUID uuid) {
        return deleteBank(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Deletes the bank owned by the specified player in the given world.
     *
     * @param uuid  the UUID of the bank owner
     * @param world the world scope of the bank
     * @return a future that completes with {@code true} if the bank was deleted
     * @since 0.3.0
     */
    default CompletableFuture<Boolean> deleteBank(final UUID uuid, final World world) {
        return deleteBank(Bukkit.getOfflinePlayer(uuid), world);
    }
}
