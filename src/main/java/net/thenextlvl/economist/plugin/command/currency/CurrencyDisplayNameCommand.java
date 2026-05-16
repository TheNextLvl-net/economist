package net.thenextlvl.economist.plugin.command.currency;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.currency.Currency;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.argument.CurrencyArgumentType;
import net.thenextlvl.economist.plugin.command.argument.LocaleArgumentType;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;

import java.util.Locale;

final class CurrencyDisplayNameCommand extends SimpleCommand {
    private CurrencyDisplayNameCommand(final EconomistPlugin plugin) {
        super(plugin, "display-name", "economist.currency.display-name");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new CurrencyDisplayNameCommand(plugin);
        final var name = Commands.argument("name", StringArgumentType.greedyString()).executes(command);
        final var locale = Commands.argument("locale", new LocaleArgumentType())
                .then(name)
                .then(Commands.literal("clear").executes(command));
        final var singular = Commands.literal("singular").then(locale);
        final var plural = Commands.literal("plural").then(locale);
        return command.create()
                .then(Commands.argument("currency", new CurrencyArgumentType(plugin))
                        .then(singular)
                        .then(plural));
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        final var currency = context.getArgument("currency", Currency.class);
        final var locale = context.getArgument("locale", Locale.class);
        
        final var singular = context.getNodes().stream().anyMatch(node -> node.getNode().getName().equals("singular"));
        final var clear = context.getNodes().stream().anyMatch(node -> node.getNode().getName().equals("clear"));
        final boolean changed;
        if (clear) {
            changed = singular ? currency.setDisplayNameSingular(locale, null) : currency.setDisplayNamePlural(locale, null);
        } else {
            final var name = MiniMessage.miniMessage().deserialize(context.getArgument("name", String.class));
            changed = singular ? currency.setDisplayNameSingular(locale, name) : currency.setDisplayNamePlural(locale, name);
        }
        if (!changed) {
            plugin.bundle().sendMessage(sender, "nothing.changed",
                    Placeholder.parsed("currency", currency.getName()));
            return 0;
        }
        plugin.currencyController().save(plugin, currency);
        final var message = clear
                ? (singular ? "currency.updated.display-name.singular.cleared" : "currency.updated.display-name.plural.cleared")
                : (singular ? "currency.updated.display-name.singular" : "currency.updated.display-name.plural");
        plugin.bundle().sendMessage(sender, message,
                Placeholder.parsed("currency", currency.getName()),
                Placeholder.parsed("locale", locale.toLanguageTag()));
        return SINGLE_SUCCESS;
    }
}
