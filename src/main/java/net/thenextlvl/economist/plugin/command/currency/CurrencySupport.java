package net.thenextlvl.economist.plugin.command.currency;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.thenextlvl.economist.currency.Currency;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

final class CurrencySupport {
    static final int PAGE_SIZE = 10;
    static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private CurrencySupport() {
    }

    static List<Currency> ordered(final net.thenextlvl.economist.plugin.EconomistPlugin plugin) {
        return plugin.currencyController().getCurrencies()
                .sorted(Comparator.comparing(Currency::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    static Optional<Locale> locale(final CommandContext<CommandSourceStack> context) {
        final String raw;
        try {
            raw = context.getArgument("locale", String.class);
        } catch (final IllegalArgumentException ignored) {
            return Optional.empty();
        }
        if (raw.isBlank()) return Optional.empty();
        final var locale = Locale.forLanguageTag(raw.replace('_', '-'));
        return locale.getLanguage().isBlank() ? Optional.empty() : Optional.of(locale);
    }

    static Component component(final CommandContext<CommandSourceStack> context, final String argument) {
        return MINI_MESSAGE.deserialize(context.getArgument(argument, String.class));
    }

    static BigDecimal decimal(final CommandContext<CommandSourceStack> context, final String argument) {
        return BigDecimal.valueOf(context.getArgument(argument, Double.class));
    }
}
