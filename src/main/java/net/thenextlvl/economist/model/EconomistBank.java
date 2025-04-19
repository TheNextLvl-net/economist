package net.thenextlvl.economist.model;

import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.bank.Bank;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@NullMarked
public class EconomistBank extends EconomistAccount implements Bank {
    private static final EconomistPlugin plugin = JavaPlugin.getPlugin(EconomistPlugin.class);

    private UUID owner;
    private final Set<UUID> members;
    private final String name;

    public EconomistBank(String name, BigDecimal balance, @Nullable World world, UUID owner, Set<UUID> members) {
        super(balance, world, owner);
        this.members = members;
        this.owner = owner;
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
    public UUID getOwner() {
        return owner;
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
        if (getWorld().map(world -> plugin.bankController().hasBank(uuid, world))
                .orElseGet(() -> plugin.bankController().hasBank(uuid))) return false;
        this.owner = uuid;
        return true;
    }
}
