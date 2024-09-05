package net.thenextlvl.economist.command;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.economist.EconomistPlugin;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class BankCommand {
    private final EconomistPlugin plugin;

    public void register() {
        var command = Commands.literal("bank")
                .requires(stack -> stack.getSender().hasPermission("economist.bank"))
                .then(Commands.literal("balance"))
                .then(Commands.literal("create"))
                .then(Commands.literal("delete"))
                .then(Commands.literal("deposit"))
                .then(Commands.literal("info"))
                .then(Commands.literal("list"))
                .then(Commands.literal("top"))
                .then(Commands.literal("withdraw"))
                .build();
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event ->
                event.registrar().register(command, "Manage banks")));
    }
}
