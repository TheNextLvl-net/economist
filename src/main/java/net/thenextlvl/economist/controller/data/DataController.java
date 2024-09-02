package net.thenextlvl.economist.controller.data;

import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface DataController {
    boolean deleteAccount(String name);

    boolean deleteAccount(UUID uuid, @Nullable World world);
}
