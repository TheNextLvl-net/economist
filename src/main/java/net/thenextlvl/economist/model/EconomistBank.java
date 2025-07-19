package net.thenextlvl.economist.model;

import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.bank.Bank;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

@NullMarked
public class EconomistBank extends EconomistAccount implements Bank {
    private final EconomistPlugin plugin;
    private final Set<UUID> members;
    private final String name;

    public EconomistBank(EconomistPlugin plugin, String name, @Nullable World world, UUID owner, Set<UUID> members) {
        super(world, owner);
        this.plugin = plugin;
        this.members = members;
        this.name = name;
    }

    @Override
    public @Unmodifiable Set<UUID> getMembers() {
        return Set.copyOf(members);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean addMember(UUID uuid) {
        return members.add(uuid);
    }

    @Override
    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    @Override
    public boolean removeMember(UUID uuid) {
        return members.remove(uuid);
    }

    @Override
    public boolean setOwner(UUID uuid) {
        if (plugin.bankController().hasBank(uuid, world)) return false;
        this.owner = uuid;
        return true;
    }
}
