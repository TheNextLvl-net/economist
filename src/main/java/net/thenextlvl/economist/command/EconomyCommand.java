package net.thenextlvl.economist.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import core.paper.command.CustomArgumentTypes;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.EconomistPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class EconomyCommand {
    private final EconomistPlugin plugin;

    public void register() {
        var amount = Commands.argument("amount", DoubleArgumentType.doubleArg(0));
        var minimum = Commands.argument("amount", DoubleArgumentType.doubleArg(plugin.config().minimumPayment()));
        var target = Commands.argument("player", CustomArgumentTypes.cachedOfflinePlayer());
        var targets = Commands.argument("players", ArgumentTypes.players());
        var command = Commands.literal("economy")
                .requires(stack -> stack.getSender().hasPermission("economist.admin"))
                .then(Commands.literal("give")
                        .then(target.then(minimum))
                        .then(targets.then(minimum)))
                .then(reset())
                .then(Commands.literal("set")
                        .then(target.then(amount))
                        .then(targets.then(amount)))
                .then(Commands.literal("take")
                        .then(target.then(minimum))
                        .then(targets.then(minimum)))
                .build();
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event ->
                event.registrar().register(command, "Manage the economy", List.of("eco"))));
    }

    private ArgumentBuilder<CommandSourceStack, ?> reset() {
        var worldArgument = Commands.argument("world", ArgumentTypes.world());
        return Commands.literal("reset")
                .then(Commands.argument("player", CustomArgumentTypes.cachedOfflinePlayer())
                        .then(worldArgument.executes(context -> {
                            var player = context.getArgument("player", OfflinePlayer.class);
                            var world = context.getArgument("world", World.class);
                            return reset(context, List.of(player), world);
                        })).executes(context -> {
                            var player = context.getArgument("player", OfflinePlayer.class);
                            return reset(context, List.of(player), null);
                        }))
                .then(Commands.argument("players", ArgumentTypes.players())
                        .then(worldArgument.executes(context -> {
                            var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                            var resolve = players.resolve(context.getSource());
                            var world = context.getArgument("world", World.class);
                            return reset(context, resolve, world);
                        })).executes(context -> {
                            var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                            var resolve = players.resolve(context.getSource());
                            return reset(context, resolve, null);
                        }));
    }

    private int reset(CommandContext<CommandSourceStack> context, Collection<? extends OfflinePlayer> players, @Nullable World world) {
        var sender = context.getSource().getSender();
        var locale = sender instanceof Player p ? p.locale() : Locale.US;
        players.forEach(player -> (world != null
                ? plugin.economyController().tryGetAccount(player, world)
                : plugin.economyController().tryGetAccount(player))
                .thenAccept(optional -> optional.ifPresentOrElse(account -> {
                    var balance = account.setBalance(plugin.config().startBalance());
                    plugin.bundle().sendMessage(sender, world != null ? "balance.reset.world" : "balance.reset",
                            Placeholder.parsed("world", world != null ? world.key().asString() : "null"),
                            Placeholder.parsed("player", String.valueOf(player.getName())),
                            Placeholder.parsed("balance", plugin.economyController().format(balance, locale)),
                            Placeholder.parsed("symbol", plugin.economyController().getCurrencySymbol()));
                }, () -> {
                    plugin.bundle().sendMessage(sender, world != null ? "account.not-found.world" : "account.not-found",
                            Placeholder.parsed("player", String.valueOf(player.getName())),
                            Placeholder.parsed("world", world != null ? world.key().asString() : "null"));
                })));
        return Command.SINGLE_SUCCESS;
    }
}
