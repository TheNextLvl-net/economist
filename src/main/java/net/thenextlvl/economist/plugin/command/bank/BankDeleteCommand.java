package net.thenextlvl.economist.plugin.command.bank;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import core.paper.brigadier.arguments.OfflinePlayerArgumentType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;
import org.bukkit.entity.Player;

import static net.thenextlvl.economist.plugin.command.bank.BankSupport.NAME_ARGUMENT;
import static net.thenextlvl.economist.plugin.command.bank.BankSupport.OWNER_ARGUMENT;

final class BankDeleteCommand extends SimpleCommand {
    private BankDeleteCommand(final EconomistPlugin plugin) {
        super(plugin, "delete", "economist.bank.delete");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new BankDeleteCommand(plugin);
        final var owner = Commands.argument(OWNER_ARGUMENT, OfflinePlayerArgumentType.player())
                .requires(stack -> stack.getSender().hasPermission("economist.bank.delete.others"));
        final var name = Commands.argument(NAME_ARGUMENT, com.mojang.brigadier.arguments.StringArgumentType.word())
                .requires(stack -> stack.getSender().hasPermission("economist.bank.delete.others"));
        return command.create()
                .executes(command)
                .then(Commands.literal("player").then(owner.executes(command)))
                .then(Commands.literal("name").then(name.executes(command)));
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        BankSupport.resolveBankTarget(plugin, context).thenAccept(optional -> optional.ifPresentOrElse(bank ->
                plugin.bankController().deleteBank(bank).thenAccept(success -> {
                    if (!success) {
                        BankSupport.sendBankNotFound(plugin, sender, context);
                        return;
                    }
                    final var self = sender instanceof final Player player && bank.getOwner().equals(player.getUniqueId());
                    plugin.bundle().sendMessage(sender, self ? "bank.deleted.self" : "bank.deleted.other",
                            Placeholder.parsed("bank", bank.getName()),
                            Placeholder.parsed("owner", BankSupport.playerName(plugin.getServer().getOfflinePlayer(bank.getOwner()))));
                }).exceptionally(throwable -> {
                    plugin.bundle().sendMessage(sender, "operation.failed");
                    return null;
                }), () -> BankSupport.sendBankNotFound(plugin, sender, context)));
        return SINGLE_SUCCESS;
    }
}
