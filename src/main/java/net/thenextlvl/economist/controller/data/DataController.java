package net.thenextlvl.economist.controller.data;

import net.thenextlvl.economist.api.Account;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface DataController {
    boolean deleteAccount(UUID uuid, @Nullable World world);

    @Nullable Account getAccount(UUID uuid, @Nullable World world);

    boolean save(Account account);
}
