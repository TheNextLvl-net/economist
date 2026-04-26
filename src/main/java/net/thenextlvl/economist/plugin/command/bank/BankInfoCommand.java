package net.thenextlvl.economist.plugin.command.bank;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import core.paper.brigadier.arguments.OfflinePlayerArgumentType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;

import static net.thenextlvl.economist.plugin.command.bank.BankSupport.NAME_ARGUMENT;
import static net.thenextlvl.economist.plugin.command.bank.BankSupport.OWNER_ARGUMENT;

final class BankInfoCommand extends SimpleCommand {
    private BankInfoCommand(final EconomistPlugin plugin) {
        super(plugin, "info", "economist.bank.info");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new BankInfoCommand(plugin);
        final var owner = Commands.argument(OWNER_ARGUMENT, OfflinePlayerArgumentType.player())
                .requires(stack -> stack.getSender().hasPermission("economist.bank.info.others"));
        final var name = Commands.argument(NAME_ARGUMENT, StringArgumentType.word())
                .requires(stack -> stack.getSender().hasPermission("economist.bank.info.others"));
        return command.create()
                .executes(command)
                .then(Commands.literal("player").then(owner.executes(command)))
                .then(Commands.literal("name").then(name.executes(command)));
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        BankSupport.resolveBankTarget(plugin, context).thenAccept(optional -> optional.ifPresentOrElse(bank -> {
            final var currency = BankSupport.currency(plugin);
            final var owner = plugin.getServer().getOfflinePlayer(bank.getOwner());
            plugin.bundle().sendMessage(sender, "bank.info.header",
                    Placeholder.parsed("bank", bank.getName()));
            plugin.bundle().sendMessage(sender, "bank.info.owner",
                    Placeholder.parsed("owner", BankSupport.playerName(owner)));
            plugin.bundle().sendMessage(sender, "bank.info.balance",
                    Placeholder.component("balance", currency.format(bank.getBalance(currency), sender)),
                    Placeholder.component("symbol", currency.getSymbol()));
            plugin.bundle().sendMessage(sender, "bank.info.members",
                    Placeholder.parsed("members", String.valueOf(bank.getMembers().size())));
        }, () -> BankSupport.sendBankNotFound(plugin, sender, context)));
        return SINGLE_SUCCESS;
    }
}
