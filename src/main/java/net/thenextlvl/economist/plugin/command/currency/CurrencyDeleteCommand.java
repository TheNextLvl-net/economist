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

final class CurrencyDeleteCommand extends SimpleCommand {
    private CurrencyDeleteCommand(final EconomistPlugin plugin) {
        super(plugin, "delete", "economist.currency.delete");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new CurrencyDeleteCommand(plugin);
        return command.create()
                .then(Commands.argument("currency", new CurrencyArgumentType(plugin)).executes(command));
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        final var currency = context.getArgument("currency", Currency.class);
        if (plugin.currencyController().getDefaultCurrency().getName().equalsIgnoreCase(currency.getName())) {
            plugin.bundle().sendMessage(sender, "currency.delete.default",
                    Placeholder.parsed("currency", currency.getName()));
            return 0;
        }
        final var success = plugin.currencyController().delete(plugin, currency.getName());
        if (!success) {
            plugin.bundle().sendMessage(sender, "operation.failed");
            return 0;
        }
        plugin.bundle().sendMessage(sender, "currency.deleted",
                Placeholder.parsed("currency", currency.getName()));
        return SINGLE_SUCCESS;
    }
}
