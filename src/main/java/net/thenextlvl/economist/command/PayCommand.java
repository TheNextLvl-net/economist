package net.thenextlvl.economist.command;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.economist.EconomistPlugin;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class PayCommand {
    private final EconomistPlugin plugin;

    public void register() {
        var command = Commands.literal("pay")
                .requires(stack -> stack.getSender().hasPermission("economist.pay"))
                .build();
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event ->
                event.registrar().register(command, "Pay another player")));
    }
}
