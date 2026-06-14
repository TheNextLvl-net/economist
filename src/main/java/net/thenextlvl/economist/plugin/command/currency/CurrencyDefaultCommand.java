package net.thenextlvl.economist.plugin.command.currency;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.currency.Currency;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.argument.CurrencyArgumentType;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;

final class CurrencyDefaultCommand extends SimpleCommand {
    private CurrencyDefaultCommand(final EconomistPlugin plugin) {
        super(plugin, "default", "economist.currency.default");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new CurrencyDefaultCommand(plugin);
        final var currency = Commands.argument("currency", new CurrencyArgumentType(plugin));
        return command.create()
                .then(currency.executes(command))
                .executes(command);
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        final var currency = tryGetArgument(context, "currency", Currency.class);
        if (currency.isEmpty()) {
            final var defaultCurrency = plugin.currencyController().getDefaultCurrency();
            plugin.bundle().sendMessage(sender, "currency.default.current",
                    Placeholder.parsed("currency", defaultCurrency.getName()),
                    Placeholder.component("symbol", defaultCurrency.getSymbol()));
            return SINGLE_SUCCESS;
        }
        final var selected = currency.orElseThrow();
        if (plugin.currencyController().getDefaultCurrency().getName().equalsIgnoreCase(selected.getName())) {
            plugin.bundle().sendMessage(sender, "nothing.changed",
                    Placeholder.parsed("currency", selected.getName()));
            return 0;
        }
        if (!plugin.currencyController().setDefaultCurrency(selected)) {
            plugin.bundle().sendMessage(sender, "operation.failed");
            return 0;
        }
        plugin.bundle().sendMessage(sender, "currency.default.updated",
                Placeholder.parsed("currency", selected.getName()),
                Placeholder.component("symbol", selected.getSymbol()));
        return SINGLE_SUCCESS;
    }
}
