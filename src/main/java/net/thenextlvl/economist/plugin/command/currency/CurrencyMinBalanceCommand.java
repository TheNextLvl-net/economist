package net.thenextlvl.economist.plugin.command.currency;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.currency.Currency;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.argument.CurrencyArgumentType;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;

final class CurrencyMinBalanceCommand extends SimpleCommand {
    private CurrencyMinBalanceCommand(final EconomistPlugin plugin) {
        super(plugin, "min-balance", "economist.currency.min-balance");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new CurrencyMinBalanceCommand(plugin);
        return command.create()
                .then(Commands.argument("currency", new CurrencyArgumentType(plugin))
                        .then(Commands.argument("balance", DoubleArgumentType.doubleArg(-Double.MAX_VALUE)).executes(command)));
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        final var currency = context.getArgument("currency", Currency.class);
        final var balance = CurrencySupport.decimal(context, "balance");
        if (balance.compareTo(currency.getMaxBalance()) > 0) {
            plugin.bundle().sendMessage(sender, "currency.balance-range.invalid");
            return 0;
        }
        if (!currency.setMinBalance(balance)) {
            plugin.bundle().sendMessage(sender, "currency.unchanged",
                    Placeholder.parsed("currency", currency.getName()));
            return 0;
        }
        plugin.currencyController().save(plugin, currency);
        plugin.bundle().sendMessage(sender, "currency.updated.min-balance",
                Placeholder.parsed("currency", currency.getName()),
                Placeholder.parsed("balance", balance.toPlainString()));
        return SINGLE_SUCCESS;
    }
}
