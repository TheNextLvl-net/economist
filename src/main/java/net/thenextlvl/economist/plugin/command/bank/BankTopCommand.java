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

final class BankTopCommand extends SimpleCommand {
    private BankTopCommand(final EconomistPlugin plugin) {
        super(plugin, "top", "economist.bank-top");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new BankTopCommand(plugin);
        return command.create()
                .executes(command)
                .then(Commands.argument("page", IntegerArgumentType.integer(1)).executes(command));
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        final var page = tryGetArgument(context, "page", Integer.class).orElse(1);
        final var start = (page - 1) * BankSupport.PAGE_SIZE;
        final var currency = plugin.currencyController().getDefaultCurrency();
        plugin.bankController().loadBanks().thenAccept(banks -> {
            final var ordered = banks
                    .sorted(Comparator.comparing((Bank bank) -> bank.getBalance(currency)).reversed()
                            .thenComparing(Bank::getName, String.CASE_INSENSITIVE_ORDER))
                    .toList();
            if (ordered.isEmpty() || start >= ordered.size()) {
                plugin.bundle().sendMessage(sender, "bank.top.empty");
                return;
            }
            plugin.bundle().sendMessage(sender, "bank.top.header",
                    Placeholder.parsed("page", String.valueOf(page)));
            for (int i = 0; i < Math.min(BankSupport.PAGE_SIZE, ordered.size() - start); i++) {
                final var bank = ordered.get(start + i);
                plugin.bundle().sendMessage(sender, "bank.top.entry",
                        Placeholder.parsed("bank", bank.getName()),
                        Placeholder.parsed("rank", String.valueOf(start + i + 1)),
                        Placeholder.component("balance", currency.format(bank.getBalance(currency), sender)),
                        Placeholder.component("symbol", currency.getSymbol()));
            }
        }).exceptionally(throwable -> {
            plugin.bundle().sendMessage(sender, "operation.failed");
            return null;
        });
        return SINGLE_SUCCESS;
    }
}
