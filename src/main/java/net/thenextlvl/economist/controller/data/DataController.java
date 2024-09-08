package net.thenextlvl.economist.controller.data;

import net.thenextlvl.economist.api.Account;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface DataController {
    @Nullable Account getAccount(UUID uuid, @Nullable World world);

    Account createAccount(UUID uuid, @Nullable World world);

    List<UUID> getAccounts(@Nullable World world);

    List<Account> getOrdered(@Nullable World world, int start, int limit);

    boolean deleteAccounts(List<UUID> accounts, @Nullable World world);

    boolean save(Account account);
}
