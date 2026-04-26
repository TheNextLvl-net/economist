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
import org.bukkit.entity.Player;

import static net.thenextlvl.economist.plugin.command.bank.BankSupport.NAME_ARGUMENT;
import static net.thenextlvl.economist.plugin.command.bank.BankSupport.OWNER_ARGUMENT;

final class BankBalanceCommand extends SimpleCommand {
    private BankBalanceCommand(final EconomistPlugin plugin) {
        super(plugin, "balance", "economist.bank.balance");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new BankBalanceCommand(plugin);
        final var owner = Commands.argument(OWNER_ARGUMENT, OfflinePlayerArgumentType.player())
                .requires(stack -> stack.getSender().hasPermission("economist.bank.balance.others"));
        final var name = Commands.argument(NAME_ARGUMENT, StringArgumentType.word())
                .requires(stack -> stack.getSender().hasPermission("economist.bank.balance.others"));
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
            final var self = sender instanceof final Player player && bank.getOwner().equals(player.getUniqueId());
            plugin.bundle().sendMessage(sender, self ? "bank.balance.self" : "bank.balance.other",
                    Placeholder.parsed("bank", bank.getName()),
                    Placeholder.parsed("owner", BankSupport.playerName(plugin.getServer().getOfflinePlayer(bank.getOwner()))),
                    Placeholder.component("balance", currency.format(bank.getBalance(currency), sender)),
                    Placeholder.component("symbol", currency.getSymbol()));
        }, () -> BankSupport.sendBankNotFound(plugin, sender, context)));
        return SINGLE_SUCCESS;
    }
}
