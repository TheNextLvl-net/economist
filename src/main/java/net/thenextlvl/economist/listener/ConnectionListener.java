package net.thenextlvl.economist.listener;

import lombok.RequiredArgsConstructor;
import net.thenextlvl.economist.EconomistPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@RequiredArgsConstructor
public class ConnectionListener implements Listener {
    private final EconomistPlugin plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.economyController().tryGetAccount(event.getPlayer()).thenAccept(optional -> {
            if (optional.isEmpty()) plugin.economyController().createAccount(event.getPlayer());
        });
    }
}
