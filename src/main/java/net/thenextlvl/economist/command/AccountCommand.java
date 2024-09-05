package net.thenextlvl.economist.command;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.economist.EconomistPlugin;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class AccountCommand {
    private final EconomistPlugin plugin;

    public void register() {
        var command = Commands.literal("account")
                .requires(stack -> stack.getSender().hasPermission("economist.account"))
                // .then(Commands.literal("balance").redirect())
                .then(Commands.literal("create").requires(stack -> stack.getSender().hasPermission("economist.account.create")))
                .then(Commands.literal("delete").requires(stack -> stack.getSender().hasPermission("economist.account.delete")))
                .build();
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event ->
                event.registrar().register(command, "Manage user accounts")));
    }
}
