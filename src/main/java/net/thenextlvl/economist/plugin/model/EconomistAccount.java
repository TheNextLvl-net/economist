package net.thenextlvl.economist.plugin.model;

import net.thenextlvl.economist.Account;
import net.thenextlvl.economist.TransactionResult;
import net.thenextlvl.economist.currency.Currency;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class EconomistAccount implements Account {
    private final @Nullable World world;
    private final Map<String, BigDecimal> balance;
    private final UUID owner;

    public EconomistAccount(final UUID owner, @Nullable final World world, final ConcurrentHashMap<String, BigDecimal> balance) {
        this.owner = owner;
        this.world = world;
        this.balance = balance;
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public Optional<World> getWorld() {
        return Optional.ofNullable(world);
    }

    @Override
    public BigDecimal getBalance(final Currency currency) {
        final var bigDecimal = balance.get(currency.getName());
        return bigDecimal != null ? bigDecimal : BigDecimal.ZERO;
    }

    @Override
    public TransactionResult deposit(final Number amount, final Currency currency) {
        if (!canHold(currency)) return TransactionResult.unsupported(currency);
        return adjustBalance(amount, decimal(amount), currency);
    }

    @Override
    public TransactionResult withdraw(final Number amount, final Currency currency) {
        if (!canHold(currency)) return TransactionResult.unsupported(currency);
        return adjustBalance(amount, decimal(amount).negate(), currency);
    }

    @Override
    public TransactionResult setBalance(final Number balance, final Currency currency) {
        if (!canHold(currency)) return TransactionResult.unsupported(currency);
        final var value = decimal(balance);
        final var result = new AtomicReference<@Nullable TransactionResult>();
        this.balance.compute(currency.getName(), (ignored, current) -> {
            final var existing = current != null ? current : BigDecimal.ZERO;
            if (outsideBounds(value, currency)) {
                result.set(new TransactionResult(currency, value, existing, TransactionResult.Status.FAILURE));
                return current;
            }
            result.set(new TransactionResult(currency, value, value, TransactionResult.Status.SUCCESS));
            return value;
        });
        return Objects.requireNonNull(result.get(), "Result is null");
    }

    @Override
    public boolean canHold(final Currency currency) {
        return !currency.getName().isBlank();
    }

    public ConcurrentHashMap<String, BigDecimal> balances() {
        return balance instanceof final ConcurrentHashMap<String, BigDecimal> concurrentHashMap
                ? concurrentHashMap
                : new ConcurrentHashMap<>(balance);
    }

    private static BigDecimal decimal(final Number number) {
        return number instanceof final BigDecimal decimal ? decimal : new BigDecimal(number.toString());
    }

    private static boolean outsideBounds(final BigDecimal balance, final Currency currency) {
        return balance.compareTo(currency.getMinBalance()) < 0
                || balance.compareTo(currency.getMaxBalance()) > 0;
    }

    private TransactionResult adjustBalance(final Number amount, final BigDecimal delta, final Currency currency) {
        final var result = new AtomicReference<@Nullable TransactionResult>();
        balance.compute(currency.getName(), (ignored, current) -> {
            final var existing = current != null ? current : BigDecimal.ZERO;
            final var updated = existing.add(delta);
            if (outsideBounds(updated, currency)) {
                result.set(new TransactionResult(currency, amount, existing, TransactionResult.Status.FAILURE));
                return current;
            }
            result.set(new TransactionResult(currency, amount, updated, TransactionResult.Status.SUCCESS));
            return updated;
        });
        return Objects.requireNonNull(result.get(), "Result is null");
    }
}
