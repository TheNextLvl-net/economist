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

final class CurrencyStarterBalanceCommand extends SimpleCommand {
    private CurrencyStarterBalanceCommand(final EconomistPlugin plugin) {
        super(plugin, "starter-balance", "economist.currency.starter-balance");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new CurrencyStarterBalanceCommand(plugin);
        return command.create()
                .then(Commands.argument("currency", new CurrencyArgumentType(plugin))
                        .then(Commands.argument("balance", DoubleArgumentType.doubleArg(-Double.MAX_VALUE)).executes(command)));
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        final var currency = context.getArgument("currency", Currency.class);
        final var balance = BigDecimal.valueOf(context.getArgument("balance", double.class));
        if (currency.getMinBalance().map(minBalance -> balance.compareTo(minBalance) < 0).orElse(false)
                || currency.getMaxBalance().map(maxBalance -> balance.compareTo(maxBalance) > 0).orElse(false)) {
            plugin.bundle().sendMessage(sender, "currency.starter-balance.invalid");
            return 0;
        }
        if (!currency.setStarterBalance(balance)) {
            plugin.bundle().sendMessage(sender, "nothing.changed",
                    Placeholder.parsed("currency", currency.getName()));
            return 0;
        }
        plugin.currencyController().save(plugin, currency);
        plugin.bundle().sendMessage(sender, "currency.updated.starter-balance",
                Placeholder.parsed("currency", currency.getName()),
                Placeholder.parsed("balance", balance.toPlainString()));
        return SINGLE_SUCCESS;
    }
}
