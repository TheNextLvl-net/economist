package net.thenextlvl.economist.controller.data;

import net.thenextlvl.economist.api.Account;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@NullMarked
public interface DataController {
    @Nullable
    Account getAccount(UUID uuid, @Nullable World world);

    Set<Account> getAccounts(@Nullable World world);

    Account createAccount(UUID uuid, @Nullable World world);

    BigDecimal getTotalBalance(@Nullable World world);

    List<Account> getOrdered(@Nullable World world, int start, int limit);

    Set<UUID> getAccountOwners(@Nullable World world);

    boolean deleteAccounts(List<UUID> accounts, @Nullable World world);

    boolean save(Account account);
}
