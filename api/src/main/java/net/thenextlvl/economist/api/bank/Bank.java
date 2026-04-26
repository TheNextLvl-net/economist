package net.thenextlvl.economist.api.bank;

import net.thenextlvl.economist.api.Account;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.Set;
import java.util.UUID;

@NullMarked
public interface Bank extends Account {
    @Unmodifiable
    Set<UUID> getMembers();

    String getName();

    default boolean addMember(final OfflinePlayer player) {
        return addMember(player.getUniqueId());
    }

    boolean addMember(UUID uuid);

    default boolean isMember(final OfflinePlayer player) {
        return isMember(player.getUniqueId());
    }

    boolean isMember(UUID uuid);

    default boolean removeMember(final OfflinePlayer player) {
        return removeMember(player.getUniqueId());
    }

    boolean removeMember(UUID uuid);

    default boolean setOwner(final OfflinePlayer player) {
        return setOwner(player.getUniqueId());
    }

    boolean setOwner(UUID uuid);
}
