package net.thenextlvl.economist.plugin.listener;

import net.thenextlvl.economist.plugin.EconomistPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {
    private final EconomistPlugin plugin;

    public ConnectionListener(final EconomistPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (!plugin.config.accounts.autoCreate) return;
        plugin.economyController().resolveAccount(event.getPlayer()).thenAccept(optional -> {
            if (optional.isEmpty()) plugin.economyController().createAccount(event.getPlayer());
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        plugin.economyController().getAccounts()
                .filter(account -> account.getOwner().equals(event.getPlayer().getUniqueId()))
                .forEach(plugin.economyController()::save);
    }
}
