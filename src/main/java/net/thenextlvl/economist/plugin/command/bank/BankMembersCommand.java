package net.thenextlvl.economist.plugin.command.bank;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import core.paper.brigadier.arguments.OfflinePlayerArgumentType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.bank.Bank;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Set;

import static net.thenextlvl.economist.plugin.command.bank.BankSupport.BANK_ARGUMENT;
import static net.thenextlvl.economist.plugin.command.bank.BankSupport.NAME_ARGUMENT;
import static net.thenextlvl.economist.plugin.command.bank.BankSupport.OWNER_ARGUMENT;

final class BankMembersCommand extends SimpleCommand {
    private BankMembersCommand(final EconomistPlugin plugin) {
        super(plugin, "members", "economist.bank.manage");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new BankMembersCommand(plugin);
        final var targetPlayer = Commands.argument("member", OfflinePlayerArgumentType.player());
        final var targetBank = Commands.argument(BANK_ARGUMENT, StringArgumentType.word())
                .requires(stack -> stack.getSender().hasPermission("economist.bank.manage.others"));
        final var owner = Commands.argument(OWNER_ARGUMENT, OfflinePlayerArgumentType.player())
                .requires(stack -> stack.getSender().hasPermission("economist.bank.info.others"));
        final var name = Commands.argument(NAME_ARGUMENT, StringArgumentType.word())
                .requires(stack -> stack.getSender().hasPermission("economist.bank.info.others"));
        return command.create()
                .executes(command)
                .then(Commands.literal("player").then(owner.executes(command)))
                .then(Commands.literal("name").then(name.executes(command)))
                .then(Commands.literal("add")
                        .then(targetPlayer.executes(command)
                                .then(Commands.literal("name").then(targetBank.executes(command)))))
                .then(Commands.literal("remove")
                        .then(targetPlayer.executes(command)
                                .then(Commands.literal("name").then(targetBank.executes(command)))))
                .then(Commands.literal("owner")
                        .then(targetPlayer.executes(command)
                                .then(Commands.literal("name").then(targetBank.executes(command)))));
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        final var action = context.getNodes().stream()
                .map(node -> node.getNode().getName())
                .filter(name -> Set.of("add", "remove", "owner").contains(name))
                .findFirst()
                .orElse("list");

        BankSupport.resolveMemberBank(plugin, context).thenAccept(optional -> optional.ifPresentOrElse(bank -> {
            switch (action) {
                case "add" -> mutateMember(sender, context, bank, Mutation.ADD);
                case "remove" -> mutateMember(sender, context, bank, Mutation.REMOVE);
                case "owner" -> mutateMember(sender, context, bank, Mutation.OWNER);
                default -> listMembers(sender, bank);
            }
        }, () -> BankSupport.sendBankNotFound(plugin, sender, context)));
        return SINGLE_SUCCESS;
    }

    private void listMembers(final CommandSender sender, final Bank bank) {
        plugin.bundle().sendMessage(sender, "bank.members.header",
                Placeholder.parsed("bank", bank.getName()));
        if (bank.getMembers().isEmpty()) {
            plugin.bundle().sendMessage(sender, "bank.members.empty");
            return;
        }
        bank.getMembers().stream()
                .map(plugin.getServer()::getOfflinePlayer)
                .map(BankSupport::playerName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(member -> plugin.bundle().sendMessage(sender, "bank.members.entry",
                        Placeholder.parsed("member", member)));
    }

    private void mutateMember(final CommandSender sender, final CommandContext<CommandSourceStack> context,
                              final Bank bank, final Mutation mutation) {
        if (!BankSupport.canManageBank(sender, context, bank)) {
            plugin.bundle().sendMessage(sender, "bank.access.denied");
            return;
        }
        final var target = context.getArgument("member", OfflinePlayer.class);
        final boolean success = switch (mutation) {
            case ADD -> bank.addMember(target);
            case REMOVE -> bank.removeMember(target);
            case OWNER -> bank.setOwner(target);
        };
        if (!success) {
            plugin.bundle().sendMessage(sender, "operation.failed");
            return;
        }
        final var message = switch (mutation) {
            case ADD -> "bank.members.add";
            case REMOVE -> "bank.members.remove";
            case OWNER -> "bank.members.owner";
        };
        plugin.bundle().sendMessage(sender, message,
                Placeholder.parsed("bank", bank.getName()),
                Placeholder.parsed("member", BankSupport.playerName(target)));
    }

    private enum Mutation {
        ADD, REMOVE, OWNER
    }
}
