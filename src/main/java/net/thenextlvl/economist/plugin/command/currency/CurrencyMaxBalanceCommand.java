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

import java.math.BigDecimal;

final class CurrencyMaxBalanceCommand extends SimpleCommand {
    private CurrencyMaxBalanceCommand(final EconomistPlugin plugin) {
        super(plugin, "max-balance", "economist.currency.max-balance");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new CurrencyMaxBalanceCommand(plugin);
        return command.create()
                .then(Commands.argument("currency", new CurrencyArgumentType(plugin))
                        .then(Commands.literal("clear").executes(command::clear))
                        .then(Commands.argument("balance", DoubleArgumentType.doubleArg(-Double.MAX_VALUE)).executes(command)));
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        final var currency = context.getArgument("currency", Currency.class);
        final var balance = BigDecimal.valueOf(context.getArgument("balance", double.class));
        if (currency.getMinBalance().map(minBalance -> balance.compareTo(minBalance) < 0).orElse(false)) {
            plugin.bundle().sendMessage(sender, "currency.balance-range.invalid");
            return 0;
        }
        if (!currency.setMaxBalance(balance)) {
            plugin.bundle().sendMessage(sender, "nothing.changed",
                    Placeholder.parsed("currency", currency.getName()));
            return 0;
        }
        plugin.currencyController().save(plugin, currency);
        plugin.bundle().sendMessage(sender, "currency.updated.max-balance",
                Placeholder.parsed("currency", currency.getName()),
                Placeholder.parsed("balance", balance.toPlainString()));
        return SINGLE_SUCCESS;
    }

    private int clear(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        final var currency = context.getArgument("currency", Currency.class);
        if (!currency.setMaxBalance(null)) {
            plugin.bundle().sendMessage(sender, "nothing.changed",
                    Placeholder.parsed("currency", currency.getName()));
            return 0;
        }
        plugin.currencyController().save(plugin, currency);
        plugin.bundle().sendMessage(sender, "currency.updated.max-balance.cleared",
                Placeholder.parsed("currency", currency.getName()));
        return SINGLE_SUCCESS;
    }
}
