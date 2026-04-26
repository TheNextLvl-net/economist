package net.thenextlvl.economist.plugin.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.thenextlvl.economist.plugin.model.LanguageTags;
import org.jspecify.annotations.NullMarked;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class LocaleArgumentType implements CustomArgumentType.Converted<Locale, String> {
    @Override
    public Locale convert(final String nativeType) {
        final var locale = LanguageTags.getLocale(nativeType);
        if (locale != null) return locale;
        throw new NullPointerException("Unknown language: " + nativeType);
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        LanguageTags.getLanguages()
                .map(entry -> StringArgumentType.escapeIfRequired(entry.getValue()))
                .filter(name -> name.toLowerCase().contains(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
