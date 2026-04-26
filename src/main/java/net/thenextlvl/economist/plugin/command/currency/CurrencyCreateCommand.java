package net.thenextlvl.economist.plugin.command.currency;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.currency.CurrencyData;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;

final class CurrencyCreateCommand extends SimpleCommand {
    private CurrencyCreateCommand(final EconomistPlugin plugin) {
        super(plugin, "create", "economist.currency.create");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new CurrencyCreateCommand(plugin);
        final var digits = Commands.argument("fractional-digits", IntegerArgumentType.integer(0));
        final var symbol = Commands.argument("symbol", StringArgumentType.greedyString());
        return command.create()
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(command)
                        .then(Commands.argument("fractional-digits", IntegerArgumentType.integer(0))
                                .executes(command))
                        .then(Commands.argument("symbol", StringArgumentType.string())
                                .executes(command)
                                .then(digits.executes(command)))
                        .then(symbol.executes(command)));
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        final var name = context.getArgument("name", String.class);
        final var digits = tryGetArgument(context, "fractional-digits", Integer.class)
                .orElse(plugin.currencyController().getDefaultCurrency().getFractionalDigits());
        final var symbol = tryGetArgument(context, "symbol", String.class)
                .map(CurrencySupport.MINI_MESSAGE::deserialize)
                .orElse(plugin.currencyController().getDefaultCurrency().getSymbol());
        try {
            final var currency = plugin.currencyController().createCurrency(CurrencyData.of(name, symbol, digits));
            plugin.currencyController().save(plugin, currency);
            plugin.bundle().sendMessage(sender, "currency.created",
                    Placeholder.parsed("currency", name),
                    Placeholder.component("symbol", symbol),
                    Placeholder.parsed("digits", String.valueOf(digits)));
        } catch (final IllegalArgumentException exception) {
            plugin.bundle().sendMessage(sender, "currency.exists",
                    Placeholder.parsed("currency", name));
        }
        return SINGLE_SUCCESS;
    }
}
