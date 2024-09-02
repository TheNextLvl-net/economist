package net.thenextlvl.economist.command;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.economist.EconomistPlugin;

import java.util.List;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class BalanceCommand {
    private final EconomistPlugin plugin;

    public void register() {
        var command = Commands.literal("balance")
                .requires(stack -> stack.getSender().hasPermission("economist.admin"))
                .build();
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event ->
                event.registrar().register(command, "Display a players balance", List.of("money"))));
    }
}
