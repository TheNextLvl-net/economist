package net.thenextlvl.economist.plugin.controller;

import net.thenextlvl.economist.bank.Bank;
import net.thenextlvl.economist.bank.BankController;
import net.thenextlvl.economist.currency.CurrencyController;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class EconomistBankController implements BankController {
    private final Map<String, Bank> banksByName = new ConcurrentHashMap<>();
    private final EconomistPlugin plugin;

    public EconomistBankController(final EconomistPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CurrencyController getCurrencyController() {
        return plugin.currencyController();
    }

    @Override
    public Stream<Bank> getBanks() {
        return banksByName.values().stream();
    }

    @Override
    public Stream<Bank> getBanks(final World world) {
        throw unsupportedWorlds(world);
    }

    @Override
    public Optional<Bank> getBank(final String name) {
        return Optional.ofNullable(banksByName.get(key(name)));
    }

    @Override
    public Optional<Bank> getBank(final OfflinePlayer player) {
        return banksByName.values().stream()
                .filter(bank -> bank.getOwner().equals(player.getUniqueId()))
                .findFirst();
    }

    @Override
    public Optional<Bank> getBank(final OfflinePlayer player, final World world) {
        throw unsupportedWorlds(world);
    }

    @Override
    public CompletableFuture<Stream<Bank>> loadBanks() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return plugin.dataController().getBanks().stream()
                        .map(this::cache);
            } catch (final SQLException exception) {
                throw new RuntimeException("Failed to load banks", exception);
            }
        });
    }

    @Override
    public CompletableFuture<Stream<Bank>> loadBanks(final World world) {
        throw unsupportedWorlds(world);
    }

    @Override
    public CompletableFuture<Optional<Bank>> loadBank(final String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Optional.ofNullable(plugin.dataController().getBank(name)).map(this::cache);
            } catch (final SQLException exception) {
                throw new RuntimeException("Failed to load bank", exception);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<Bank>> loadBank(final OfflinePlayer player) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Optional.ofNullable(plugin.dataController().getBank(player.getUniqueId())).map(this::cache);
            } catch (final SQLException exception) {
                throw new RuntimeException("Failed to load bank", exception);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<Bank>> loadBank(final OfflinePlayer player, final World world) {
        throw unsupportedWorlds(world);
    }

    @Override
    public CompletableFuture<Bank> createBank(final OfflinePlayer player, final String name) {
        return CompletableFuture.supplyAsync(() -> createBankInternal(player.getUniqueId(), name));
    }

    @Override
    public CompletableFuture<Bank> createBank(final OfflinePlayer player, final String name, final World world) {
        throw unsupportedWorlds(world);
    }

    @Override
    public CompletableFuture<Boolean> deleteBank(final OfflinePlayer player) {
        return CompletableFuture.supplyAsync(() -> deleteBankInternal(player.getUniqueId()));
    }

    @Override
    public CompletableFuture<Boolean> deleteBank(final OfflinePlayer player, final World world) {
        throw unsupportedWorlds(world);
    }

    @Override
    public CompletableFuture<Boolean> deleteBank(final String name) {
        return CompletableFuture.supplyAsync(() -> deleteBankInternal(name));
    }

    public void save() {
        banksByName.values().forEach(this::save);
        banksByName.clear();
    }

    public void save(final Bank bank) {
        try {
            plugin.dataController().save(bank);
            evict(bank);
        } catch (final SQLException exception) {
            plugin.getComponentLogger().error("Failed to save bank {}", bank.getName(), exception);
        }
    }

    private Bank createBankInternal(final UUID owner, final String name) {
        final var normalized = normalizedName(name);
        if (getBank(owner).isPresent() || getBank(normalized).isPresent()) {
            throw new IllegalStateException("Bank already exists");
        }
        try {
            if (plugin.dataController().getBank(owner) != null || plugin.dataController().getBank(normalized) != null) {
                throw new IllegalStateException("Bank already exists");
            }
            return cache(plugin.dataController().createBank(owner, normalized));
        } catch (final SQLException exception) {
            throw new RuntimeException("Failed to create bank", exception);
        }
    }

    private boolean deleteBankInternal(final UUID owner) {
        getBank(owner).ifPresent(bank -> banksByName.remove(key(bank.getName()), bank));
        try {
            return plugin.dataController().deleteBank(owner);
        } catch (final SQLException exception) {
            throw new RuntimeException("Failed to delete bank", exception);
        }
    }

    private boolean deleteBankInternal(final String name) {
        banksByName.remove(key(name));
        try {
            return plugin.dataController().deleteBank(normalizedName(name));
        } catch (final SQLException exception) {
            throw new RuntimeException("Failed to delete bank", exception);
        }
    }

    private Bank cache(final Bank bank) {
        final var key = key(bank.getName());
        return Objects.requireNonNullElse(banksByName.putIfAbsent(key, bank), bank);
    }

    private void evict(final Bank bank) {
        banksByName.remove(key(bank.getName()), bank);
    }

    private static String key(final String name) {
        return name.toLowerCase(java.util.Locale.ROOT);
    }

    private static String normalizedName(final String name) {
        return name.trim();
    }

    private static UnsupportedOperationException unsupportedWorlds(@Nullable final World world) {
        return new UnsupportedOperationException("Banks do not support world scoping");
    }
}
