package net.thenextlvl.economist.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import core.paper.command.WrappedArgumentType;
import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.currency.Currency;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class CurrencyArgument extends WrappedArgumentType<String, Currency> {
    public CurrencyArgument(EconomistPlugin plugin) {
        super(StringArgumentType.string(), (reader, type) -> {
            return plugin.currencyHolder().getCurrency(type).orElseThrow(() ->
                    new IllegalArgumentException("Unknown currency '" + type + "'"));
        }, (context, builder) -> {
            plugin.currencyHolder().getCurrencies().stream()
                    .map(Currency::getName)
                    .filter(s -> s.toLowerCase().contains(builder.getRemainingLowerCase()))
                    .forEach(builder::suggest);
            return builder.buildFuture();
        });
    }
}
