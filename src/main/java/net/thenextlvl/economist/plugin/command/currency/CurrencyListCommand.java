package net.thenextlvl.economist.plugin.command.currency;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.currency.Currency;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Locale;

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
        final var ordered = plugin.currencyController().getCurrencies()
                .sorted(Comparator.comparing(Currency::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        if (ordered.isEmpty()) {
            plugin.bundle().sendMessage(sender, "currency.list.empty");
            return 0;
        }
        final var defaultCurrency = plugin.currencyController().getDefaultCurrency();
        plugin.bundle().sendMessage(sender, "currency.list.header",
                Placeholder.parsed("count", String.valueOf(ordered.size())));
        ordered.forEach(currency -> {
            final var key = defaultCurrency.getName().equalsIgnoreCase(currency.getName())
                    ? "currency.list.entry.default"
                    : "currency.list.entry";
            plugin.bundle().sendMessage(sender, key,
                    Placeholder.parsed("currency", currency.getName().toUpperCase(Locale.ROOT)),
                    Placeholder.component("symbol", currency.getSymbol()),
                    Placeholder.component("amount", currency.format(0, sender)),
                    Placeholder.parsed("digits", String.valueOf(currency.getFractionalDigits())),
                    Placeholder.component("bounds", bounds(currency)));
        });
        return SINGLE_SUCCESS;
    }

    private static Component bounds(final Currency currency) {
        final var min = currency.getMinBalance();
        final var max = currency.getMaxBalance();
        if (min.isEmpty() && max.isEmpty()) return Component.empty();
        if (min.isPresent() && max.isPresent()) {
            return Component.text(", min " + decimal(min.get()) + ", max " + decimal(max.get()));
        }
        return min.map(bigDecimal -> Component.text(", min " + decimal(bigDecimal)))
                .orElseGet(() -> Component.text(", max " + decimal(max.orElseThrow())));
    }

    private static String decimal(final BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}
