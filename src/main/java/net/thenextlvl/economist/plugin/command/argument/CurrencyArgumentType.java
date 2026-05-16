package net.thenextlvl.economist.plugin.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.thenextlvl.economist.currency.Currency;
import net.thenextlvl.economist.plugin.EconomistPlugin;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class CurrencyArgumentType implements CustomArgumentType.Converted<Currency, String> {
    private final EconomistPlugin plugin;
    private final SimpleCommandExceptionType errorUnknownCurrency;

    public CurrencyArgumentType(final EconomistPlugin plugin) {
        this.plugin = plugin;
        this.errorUnknownCurrency = new SimpleCommandExceptionType(
                MessageComponentSerializer.message().serialize(plugin.bundle().component("argument.currency.unknown", Locale.US))
        );
    }


    @Override
    public Currency convert(final String nativeType) throws CommandSyntaxException {
        return plugin.currencyController().getCurrency(nativeType)
                .orElseThrow(errorUnknownCurrency::create);
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        plugin.currencyController().getCurrencies()
                .map(Currency::getName)
                .map(StringArgumentType::escapeIfRequired)
                .filter(s -> s.contains(builder.getRemaining()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
