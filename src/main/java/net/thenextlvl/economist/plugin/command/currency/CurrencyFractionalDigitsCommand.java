package net.thenextlvl.economist.plugin.command.currency;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.currency.Currency;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.argument.CurrencyArgumentType;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;

final class CurrencyFractionalDigitsCommand extends SimpleCommand {
    private CurrencyFractionalDigitsCommand(final EconomistPlugin plugin) {
        super(plugin, "fractional-digits", "economist.currency.fractional-digits");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new CurrencyFractionalDigitsCommand(plugin);
        return command.create()
                .then(Commands.argument("currency", new CurrencyArgumentType(plugin))
                        .then(Commands.argument("digits", IntegerArgumentType.integer(0)).executes(command)));
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        final var currency = context.getArgument("currency", Currency.class);
        final var digits = context.getArgument("digits", Integer.class);
        if (!currency.setFractionalDigits(digits)) {
            plugin.bundle().sendMessage(sender, "nothing.changed",
                    Placeholder.parsed("currency", currency.getName()));
            return 0;
        }
        plugin.currencyController().save(plugin, currency);
        plugin.bundle().sendMessage(sender, "currency.updated.fractional-digits",
                Placeholder.parsed("currency", currency.getName()),
                Placeholder.parsed("digits", String.valueOf(digits)));
        return SINGLE_SUCCESS;
    }
}
