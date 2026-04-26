package net.thenextlvl.economist.plugin.command.currency;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;

final class CurrencyListCommand extends SimpleCommand {
    private CurrencyListCommand(final EconomistPlugin plugin) {
        super(plugin, "list", "economist.currency.list");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new CurrencyListCommand(plugin);
        return command.create()
                .executes(command);
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        final var ordered = CurrencySupport.ordered(plugin);
        if (ordered.isEmpty()) {
            plugin.bundle().sendMessage(sender, "currency.list.empty");
            return 0;
        }
        plugin.bundle().sendMessage(sender, "currency.list.header");
        ordered.forEach(currency ->
                plugin.bundle().sendMessage(sender, "currency.list.entry",
                        Placeholder.parsed("currency", currency.getName()),
                        Placeholder.component("symbol", currency.getSymbol()),
                        Placeholder.parsed("digits", String.valueOf(currency.getFractionalDigits()))));
        return SINGLE_SUCCESS;
    }
}
