package net.thenextlvl.economist.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import core.paper.command.CustomArgumentTypes;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.economist.EconomistPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class PayCommand {
    private final EconomistPlugin plugin;

    public void register() {
        var amountArgument = DoubleArgumentType.doubleArg(plugin.config().minimumPayment());
        var command = Commands.literal("pay")
                .requires(stack -> stack.getSender().hasPermission("economist.pay"))
                .then(Commands.argument("player", CustomArgumentTypes.cachedOfflinePlayer())
                        .then(Commands.argument("amount", amountArgument).executes(context -> {
                            var player = context.getArgument("player", OfflinePlayer.class);
                            return pay(context, List.of(player), null);
                        }).then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> stack.getSender().hasPermission("economist.pay.world"))
                                .executes(context -> {
                                    var player = context.getArgument("player", OfflinePlayer.class);
                                    var world = context.getArgument("world", World.class);
                                    return pay(context, List.of(player), world);
                                }))))
                .then(Commands.argument("players", ArgumentTypes.players())
                        .then(Commands.argument("amount", amountArgument).executes(context -> {
                            var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                            return pay(context, List.copyOf(players.resolve(context.getSource())), null);
                        }).then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> stack.getSender().hasPermission("economist.pay.world"))
                                .executes(context -> {
                                    var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                                    var world = context.getArgument("world", World.class);
                                    return pay(context, List.copyOf(players.resolve(context.getSource())), world);
                                }))))
                .build();
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event ->
                event.registrar().register(command, "Pay another player")));
    }

    private int pay(CommandContext<CommandSourceStack> context, List<? extends OfflinePlayer> players, @Nullable World world) {
        return 0;
    }
}
