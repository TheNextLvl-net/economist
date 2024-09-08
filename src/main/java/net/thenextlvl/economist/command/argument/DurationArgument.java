package net.thenextlvl.economist.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import core.paper.command.ComponentCommandExceptionType;
import core.paper.command.WrappedArgumentType;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class DurationArgument extends WrappedArgumentType<String, Duration> {
    private static final Map<String, ChronoUnit> UNITS = new LinkedHashMap<>();

    static {
        UNITS.put("d", ChronoUnit.DAYS);
        UNITS.put("h", ChronoUnit.HOURS);
        UNITS.put("m", ChronoUnit.MINUTES);
        UNITS.put("s", ChronoUnit.SECONDS);
    }

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

    public static DurationArgument duration() {
        return duration(Duration.ZERO);
    }

    public static DurationArgument duration(Duration min) {
        return duration(min, ChronoUnit.FOREVER.getDuration());
    }

    public static DurationArgument duration(Duration min, Duration max) {
        return new DurationArgument(min, max);
    }

    private DurationArgument(Duration min, Duration max) {
        super(StringArgumentType.word(), (reader, type) -> {
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
        }, (context, builder) -> {
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
        });
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
}
