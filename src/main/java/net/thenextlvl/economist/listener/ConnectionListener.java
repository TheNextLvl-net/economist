package net.thenextlvl.economist.listener;

import net.thenextlvl.economist.EconomistPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ConnectionListener implements Listener {
    private final EconomistPlugin plugin;

    public ConnectionListener(EconomistPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.config.accounts.autoCreate) return;
        plugin.economyController().tryGetAccount(event.getPlayer()).thenAccept(optional -> {
            if (optional.isEmpty()) plugin.economyController().createAccount(event.getPlayer());
        });
    }
}
