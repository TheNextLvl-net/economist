package net.thenextlvl.economist.model;

import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.bank.Bank;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@NullMarked
public class EconomistBank extends EconomistAccount implements Bank {
    private final Set<UUID> members;
    private final String name;

    public EconomistBank(EconomistPlugin plugin, String name, @Nullable World world, UUID owner, Set<UUID> members) {
        super(plugin, world, owner);
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
        if (!members.add(uuid)) return false;
        plugin.bankController().markDirty(this);
        return true;
    }

    @Override
    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    @Override
    public boolean removeMember(UUID uuid) {
        if (!members.remove(uuid)) return false;
        plugin.bankController().markDirty(this);
        return true;
    }

    @Override
    public boolean setOwner(UUID uuid) {
        if (this.owner.equals(uuid)) return false;
        if (plugin.bankController().hasBank(uuid, world)) return false;
        plugin.bankController().markDirty(this);
        this.owner = uuid;
        return true;
    }

    @Override
    protected void markDirty() {
        plugin.bankController().markDirty(this);
        lastUpdate = Instant.now();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EconomistBank that = (EconomistBank) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }

    @Override
    public String toString() {
        return "EconomistBank{" +
               "members=" + members +
               ", name='" + name + '\'' +
               ", world=" + world +
               ", balances=" + balances +
               ", owner=" + owner +
               '}';
    }
}
