package net.thenextlvl.economist.plugin.command.currency;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.currency.Currency;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.argument.CurrencyArgumentType;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;

final class CurrencySymbolCommand extends SimpleCommand {
    private CurrencySymbolCommand(final EconomistPlugin plugin) {
        super(plugin, "symbol", "economist.currency.symbol");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new CurrencySymbolCommand(plugin);
        return command.create()
                .then(Commands.argument("currency", new CurrencyArgumentType(plugin))
                        .then(Commands.argument("symbol", StringArgumentType.greedyString()).executes(command)));
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        final var currency = context.getArgument("currency", Currency.class);
        final var symbol = CurrencySupport.component(context, "symbol");
        if (!currency.setSymbol(symbol)) {
            plugin.bundle().sendMessage(sender, "nothing.changed",
                    Placeholder.parsed("currency", currency.getName()));
            return 0;
        }
        plugin.currencyController().save(plugin, currency);
        plugin.bundle().sendMessage(sender, "currency.updated.symbol",
                Placeholder.parsed("currency", currency.getName()),
                Placeholder.component("symbol", symbol));
        return SINGLE_SUCCESS;
    }
}
