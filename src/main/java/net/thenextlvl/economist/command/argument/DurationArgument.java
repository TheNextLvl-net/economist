package net.thenextlvl.economist.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import core.paper.brigadier.exceptions.ComponentCommandExceptionType;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class DurationArgument implements CustomArgumentType<Duration, String> {
    private static final Map<String, ChronoUnit> UNITS = new LinkedHashMap<>(Map.of(
            "d", ChronoUnit.DAYS,
            "h", ChronoUnit.HOURS,
            "m", ChronoUnit.MINUTES,
            "s", ChronoUnit.SECONDS
    ));

    private static final SimpleCommandExceptionType ERROR_INVALID_UNIT = new ComponentCommandExceptionType(
            Component.translatable("argument.time.invalid_unit")
    );
    private static final Dynamic2CommandExceptionType LONG_TOO_SMALL = new Dynamic2CommandExceptionType((found, min) ->
            MessageComponentSerializer.message().serialize(Component.translatable("argument.long.low",
                    Component.text(String.valueOf(min)),
                    Component.text(String.valueOf(found))
            )));
    private static final Dynamic2CommandExceptionType LONG_TOO_BIG = new Dynamic2CommandExceptionType((found, max) ->
            MessageComponentSerializer.message().serialize(Component.translatable("argument.long.big",
                    Component.text(String.valueOf(max)),
                    Component.text(String.valueOf(found))
            )));

    private final Duration min;
    private final Duration max;

    private DurationArgument(Duration min, Duration max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public Duration parse(StringReader reader) throws CommandSyntaxException {
        var type = getNativeType().parse(reader);
        var duration = Duration.ZERO;
        try {
            duration = Duration.ofMillis(Long.parseLong(type));
        } catch (NumberFormatException e) {
            var unit = UNITS.entrySet().stream()
                    .filter(entry -> type.endsWith(entry.getKey()))
                    .findFirst()
                    .orElseThrow(() -> ERROR_INVALID_UNIT.createWithContext(reader));
            var s = type.substring(0, type.length() - unit.getKey().length());
            try {
                duration = Duration.of(Long.parseLong(s), unit.getValue());
            } catch (NumberFormatException ignored) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidLong().createWithContext(reader, s);
            }
        }
        if (duration.compareTo(min) < 0) throw LONG_TOO_SMALL.createWithContext(reader, duration, min);
        if (duration.compareTo(max) > 0) throw LONG_TOO_BIG.createWithContext(reader, duration, max);
        return duration;
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        var reader = new StringReader(builder.getRemaining());

        try {
            reader.readLong();
        } catch (CommandSyntaxException var5) {
            return builder.buildFuture();
        }

        var offset = builder.createOffset(builder.getStart() + reader.getCursor());
        var input = offset.getRemaining().toLowerCase();

        for (var unit : UNITS.keySet()) {
            if (matchesSubStr(input, unit.toLowerCase())) {
                offset.suggest(unit);
            }
        }

        return offset.buildFuture();
    }

    private static boolean matchesSubStr(String remaining, String candidate) {
        for (int i = 0; !candidate.startsWith(remaining, i); i++) {
            int j = candidate.indexOf(46, i);
            int k = candidate.indexOf(95, i);
            if (Math.max(j, k) < 0) {
                return false;
            }

            if (j >= 0 && k >= 0) {
                i = Math.min(k, j);
            } else {
                i = j >= 0 ? j : k;
            }
        }
        return true;
    }

    public static DurationArgument duration() {
        return duration(Duration.ZERO);
    }

    public static DurationArgument duration(Duration min) {
        return duration(min, ChronoUnit.FOREVER.getDuration());
    }

    public static DurationArgument duration(Duration min, Duration max) {
        return new DurationArgument(min, max);
    }
}
