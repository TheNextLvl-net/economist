package net.thenextlvl.economist.command;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.economist.EconomistPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
@RequiredArgsConstructor
public class BankCommand {
    private final EconomistPlugin plugin;

    public void register() {
        var command = Commands.literal("bank")
                .requires(stack -> stack.getSender().hasPermission("economist.bank"))
                .then(Commands.literal("balance").requires(stack -> stack.getSender().hasPermission("economist.bank.balance")))
                .then(Commands.literal("create").requires(stack -> stack.getSender().hasPermission("economist.bank.create")))
                .then(Commands.literal("delete").requires(stack -> stack.getSender().hasPermission("economist.bank.delete")))
                .then(Commands.literal("deposit").requires(stack -> stack.getSender().hasPermission("economist.bank.deposit")))
                .then(Commands.literal("info").requires(stack -> stack.getSender().hasPermission("economist.bank.info")))
                .then(Commands.literal("list").requires(stack -> stack.getSender().hasPermission("economist.bank.list")))
                .then(Commands.literal("members").requires(stack -> stack.getSender().hasPermission("economist.bank.members")))
                .then(Commands.literal("movements").requires(stack -> stack.getSender().hasPermission("economist.bank.movements")))
                .then(Commands.literal("top").requires(stack -> stack.getSender().hasPermission("economist.bank-top")))
                .then(Commands.literal("transfer").requires(stack -> stack.getSender().hasPermission("economist.bank.transfer")))
                .then(Commands.literal("withdraw").requires(stack -> stack.getSender().hasPermission("economist.bank.withdraw")))
                .build();
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event ->
                event.registrar().register(command, "Manage bank accounts")));
    }
}
