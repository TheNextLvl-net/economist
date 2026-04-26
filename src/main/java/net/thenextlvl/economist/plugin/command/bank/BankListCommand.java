package net.thenextlvl.economist.plugin.command.bank;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.bank.Bank;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;

import java.util.Comparator;

final class BankListCommand extends SimpleCommand {
    private BankListCommand(final EconomistPlugin plugin) {
        super(plugin, "list", "economist.bank.list");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new BankListCommand(plugin);
        return command.create()
                .executes(command)
                .then(Commands.argument("page", IntegerArgumentType.integer(1)).executes(command));
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        final var page = BankSupport.findArgument(context, "page", Integer.class).orElse(1);
        final var start = (page - 1) * BankSupport.PAGE_SIZE;
        plugin.bankController().loadBanks().thenAccept(banks -> {
            final var ordered = banks
                    .sorted(Comparator.comparing(Bank::getName, String.CASE_INSENSITIVE_ORDER))
                    .toList();
            if (ordered.isEmpty() || start >= ordered.size()) {
                plugin.bundle().sendMessage(sender, "bank.list.empty");
                return;
            }
            plugin.bundle().sendMessage(sender, "bank.list.header",
                    Placeholder.parsed("page", String.valueOf(page)));
            ordered.stream().skip(start).limit(BankSupport.PAGE_SIZE).forEach(bank ->
                    plugin.bundle().sendMessage(sender, "bank.list.entry",
                            Placeholder.parsed("bank", bank.getName()),
                            Placeholder.parsed("owner", BankSupport.playerName(plugin.getServer().getOfflinePlayer(bank.getOwner())))));
        }).exceptionally(throwable -> {
            plugin.bundle().sendMessage(sender, "operation.failed");
            return null;
        });
        return SINGLE_SUCCESS;
    }
}
