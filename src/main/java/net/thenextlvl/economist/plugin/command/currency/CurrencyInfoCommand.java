package net.thenextlvl.economist.plugin.command.currency;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.currency.Currency;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.argument.CurrencyArgumentType;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;

import java.util.Locale;
import java.util.stream.Collectors;

final class CurrencyInfoCommand extends SimpleCommand {
    private CurrencyInfoCommand(final EconomistPlugin plugin) {
        super(plugin, "info", "economist.currency.info");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new CurrencyInfoCommand(plugin);
        return command.create()
                .then(Commands.argument("currency", new CurrencyArgumentType(plugin)).executes(command));
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        final var currency = context.getArgument("currency", Currency.class);
        final var singular = currency.getDisplayNameSingular(Locale.US).orElse(Component.text("-"));
        final var plural = currency.getDisplayNamePlural(Locale.US).orElse(Component.text("-"));
        final var locales = currency.toData().displayNamesSingular().keySet().stream()
                .map(Locale::toLanguageTag)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining(", "));
        plugin.bundle().sendMessage(sender, "currency.info.header",
                Placeholder.parsed("currency", currency.getName()));
        plugin.bundle().sendMessage(sender, "currency.info.symbol",
                Placeholder.component("symbol", currency.getSymbol()));
        plugin.bundle().sendMessage(sender, "currency.info.fractional-digits",
                Placeholder.parsed("digits", String.valueOf(currency.getFractionalDigits())));
        plugin.bundle().sendMessage(sender, "currency.info.min-balance",
                Placeholder.component("balance", currency.getMinBalance()
                        .<Component>map(balance -> Component.text(balance.toPlainString()))
                        .orElseGet(() -> plugin.bundle().component("currency.value.unlimited",
                                sender.getOrDefault(Identity.LOCALE, Locale.US)))));
        plugin.bundle().sendMessage(sender, "currency.info.max-balance",
                Placeholder.component("balance", currency.getMaxBalance()
                        .<Component>map(balance -> Component.text(balance.toPlainString()))
                        .orElseGet(() -> plugin.bundle().component("currency.value.unlimited",
                                sender.getOrDefault(Identity.LOCALE, Locale.US)))));
        plugin.bundle().sendMessage(sender, "currency.info.singular",
                Placeholder.component("name", singular));
        plugin.bundle().sendMessage(sender, "currency.info.plural",
                Placeholder.component("name", plural));
        plugin.bundle().sendMessage(sender, "currency.info.locales",
                Placeholder.parsed("locales", locales.isBlank() ? "-" : locales));
        return SINGLE_SUCCESS;
    }
}
