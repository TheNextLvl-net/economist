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
import net.thenextlvl.economist.api.Account;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class EconomyCommand {
    private final EconomistPlugin plugin;

    public void register() {
        var command = Commands.literal("economy")
                .requires(stack -> stack.getSender().hasPermission("economist.admin"))
                .then(give()).then(reset()).then(set()).then(take()).build();
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event ->
                event.registrar().register(command, "Manage the economy", List.of("eco"))));
    }

    private ArgumentBuilder<CommandSourceStack, ?> give() {
        return create("give", "balance.deposited", "balance.deposited.world", Account::deposit, plugin.config().minimumPayment());
    }

    private ArgumentBuilder<CommandSourceStack, ?> take() {
        return create("take", "balance.withdrawn", "balance.withdrawn.world", Account::withdraw, plugin.config().minimumPayment());
    }

    private ArgumentBuilder<CommandSourceStack, ?> set() {
        return create("set", "balance.set", "balance.set.world", Account::setBalance, -Double.MAX_VALUE);
    }

    private ArgumentBuilder<CommandSourceStack, ?> reset() {
        return Commands.literal("reset")
                .then(Commands.argument("player", CustomArgumentTypes.cachedOfflinePlayer())
                        .then(Commands.argument("world", ArgumentTypes.world()).executes(context -> {
                            var player = context.getArgument("player", OfflinePlayer.class);
                            var world = context.getArgument("world", World.class);
                            return execute(context, "balance.reset.world", List.of(player), plugin.config().startBalance(), world, Account::setBalance);
                        })).executes(context -> {
                            var player = context.getArgument("player", OfflinePlayer.class);
                            return execute(context, "balance.reset", List.of(player), plugin.config().startBalance(), null, Account::setBalance);
                        }))
                .then(Commands.argument("players", ArgumentTypes.players())
                        .then(Commands.argument("world", ArgumentTypes.world()).executes(context -> {
                            var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                            var resolve = players.resolve(context.getSource());
                            var world = context.getArgument("world", World.class);
                            return execute(context, "balance.reset.world", resolve, plugin.config().startBalance(), world, Account::setBalance);
                        })).executes(context -> {
                            var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                            var resolve = players.resolve(context.getSource());
                            return execute(context, "balance.reset", resolve, plugin.config().startBalance(), null, Account::setBalance);
                        }));
    }

    private ArgumentBuilder<CommandSourceStack, ?> create(String command, String successMessage, String successMessageWorld,
                                                          BiFunction<Account, Number, BigDecimal> function, Double minimum) {
        return Commands.literal(command)
                .then(Commands.argument("player", CustomArgumentTypes.cachedOfflinePlayer())
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(minimum))
                                .then(Commands.argument("world", ArgumentTypes.world()).executes(context -> {
                                    var player = context.getArgument("player", OfflinePlayer.class);
                                    var amount = context.getArgument("amount", Double.class);
                                    var world = context.getArgument("world", World.class);
                                    return execute(context, successMessageWorld, List.of(player), amount, world, function);
                                })).executes(context -> {
                                    var player = context.getArgument("player", OfflinePlayer.class);
                                    var amount = context.getArgument("amount", Double.class);
                                    return execute(context, successMessage, List.of(player), amount, null, function);
                                })))
                .then(Commands.argument("players", ArgumentTypes.players())
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(minimum))
                                .then(Commands.argument("world", ArgumentTypes.world()).executes(context -> {
                                    var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                                    var amount = context.getArgument("amount", Double.class);
                                    var world = context.getArgument("world", World.class);
                                    return execute(context, successMessageWorld, List.copyOf(players.resolve(context.getSource())), amount, world, function);
                                })).executes(context -> {
                                    var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                                    var amount = context.getArgument("amount", Double.class);
                                    return execute(context, successMessage, List.copyOf(players.resolve(context.getSource())), amount, null, function);
                                })));
    }

    private int execute(CommandContext<CommandSourceStack> context, String successMessage,
                        Collection<? extends OfflinePlayer> players, Number amount, @Nullable World world,
                        BiFunction<Account, Number, BigDecimal> function) {
        var sender = context.getSource().getSender();
        var locale = sender instanceof Player p ? p.locale() : Locale.US;
        if (!players.isEmpty()) players.forEach(player -> (world != null
                ? plugin.economyController().tryGetAccount(player, world)
                : plugin.economyController().tryGetAccount(player))
                .thenAccept(optional -> optional.ifPresentOrElse(account -> {
                    var balance = function.apply(account, amount);
                    plugin.bundle().sendMessage(sender, successMessage,
                            Placeholder.parsed("world", world != null ? world.key().asString() : "null"),
                            Placeholder.parsed("player", String.valueOf(player.getName())),
                            Placeholder.parsed("balance", plugin.economyController().format(balance, locale)),
                            Placeholder.parsed("amount", plugin.economyController().format(amount, locale)),
                            Placeholder.parsed("symbol", plugin.economyController().getCurrencySymbol()));
                }, () -> plugin.bundle().sendMessage(sender, world != null
                                ? (sender.equals(player) ? "account.not-found.world.self" : "account.not-found.world.other")
                                : (sender.equals(player) ? "account.not-found.self" : "account.not-found.other"),
                        Placeholder.parsed("player", String.valueOf(player.getName())),
                        Placeholder.parsed("world", world != null ? world.key().asString() : "null")))));
        else plugin.bundle().sendMessage(sender, "player.define");
        return Command.SINGLE_SUCCESS;
    }
}
