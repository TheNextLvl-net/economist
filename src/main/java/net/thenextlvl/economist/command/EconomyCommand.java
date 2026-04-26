package net.thenextlvl.economist.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import core.paper.brigadier.arguments.OfflinePlayerArgumentType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.Account;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;

@NullMarked
public class EconomyCommand {
    public static LiteralCommandNode<CommandSourceStack> create(final EconomistPlugin plugin) {
        return Commands.literal("economy")
                .requires(stack -> stack.getSender().hasPermission("economist.admin"))
                .then(give(plugin))
                .then(reset(plugin))
                .then(set(plugin))
                .then(take(plugin))
                .build();
    }

    private static ArgumentBuilder<CommandSourceStack, ?> give(final EconomistPlugin plugin) {
        return create("give", "balance.deposited", "balance.deposited.world", Account::deposit, plugin.config.minimumPayment, plugin);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> take(final EconomistPlugin plugin) {
        return create("take", "balance.withdrawn", "balance.withdrawn.world", Account::withdraw, plugin.config.minimumPayment, plugin);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> set(final EconomistPlugin plugin) {
        return create("set", "balance.set", "balance.set.world", Account::setBalance, -Double.MAX_VALUE, plugin);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> reset(final EconomistPlugin plugin) {
        return Commands.literal("reset")
                .then(Commands.argument("player", OfflinePlayerArgumentType.player())
                        .then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> plugin.config.accounts.perWorld)
                                .executes(context -> {
                                    final var player = context.getArgument("player", OfflinePlayer.class);
                                    final var world = context.getArgument("world", World.class);
                                    return execute(context, "balance.reset.world", List.of(player), plugin.config.startBalance, world, Account::setBalance, plugin);
                                })).executes(context -> {
                            final var player = context.getArgument("player", OfflinePlayer.class);
                            return execute(context, "balance.reset", List.of(player), plugin.config.startBalance, null, Account::setBalance, plugin);
                        }))
                .then(Commands.argument("players", ArgumentTypes.players())
                        .then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> plugin.config.accounts.perWorld)
                                .executes(context -> {
                                    final var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                                    final var resolve = players.resolve(context.getSource());
                                    final var world = context.getArgument("world", World.class);
                                    return execute(context, "balance.reset.world", resolve, plugin.config.startBalance, world, Account::setBalance, plugin);
                                })).executes(context -> {
                            final var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                            final var resolve = players.resolve(context.getSource());
                            return execute(context, "balance.reset", resolve, plugin.config.startBalance, null, Account::setBalance, plugin);
                        }));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> create(final String command, final String successMessage, final String successMessageWorld,
                                                                 final BiFunction<Account, Number, BigDecimal> function, final Double minimum, final EconomistPlugin plugin) {
        return Commands.literal(command)
                .then(Commands.argument("player", OfflinePlayerArgumentType.player())
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(minimum))
                                .then(Commands.argument("world", ArgumentTypes.world())
                                        .requires(stack -> plugin.config.accounts.perWorld)
                                        .executes(context -> {
                                            final var player = context.getArgument("player", OfflinePlayer.class);
                                            final var amount = context.getArgument("amount", Double.class);
                                            final var world = context.getArgument("world", World.class);
                                            return execute(context, successMessageWorld, List.of(player), amount, world, function, plugin);
                                        })).executes(context -> {
                                    final var player = context.getArgument("player", OfflinePlayer.class);
                                    final var amount = context.getArgument("amount", Double.class);
                                    return execute(context, successMessage, List.of(player), amount, null, function, plugin);
                                })))
                .then(Commands.argument("players", ArgumentTypes.players())
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(minimum))
                                .then(Commands.argument("world", ArgumentTypes.world())
                                        .requires(stack -> plugin.config.accounts.perWorld)
                                        .executes(context -> {
                                            final var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                                            final var amount = context.getArgument("amount", Double.class);
                                            final var world = context.getArgument("world", World.class);
                                            return execute(context, successMessageWorld, List.copyOf(players.resolve(context.getSource())), amount, world, function, plugin);
                                        })).executes(context -> {
                                    final var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                                    final var amount = context.getArgument("amount", Double.class);
                                    return execute(context, successMessage, List.copyOf(players.resolve(context.getSource())), amount, null, function, plugin);
                                })));
    }

    private static int execute(final CommandContext<CommandSourceStack> context, final String successMessage,
                               final Collection<? extends OfflinePlayer> players, final Number amount, @Nullable final World world,
                               final BiFunction<Account, Number, BigDecimal> function, final EconomistPlugin plugin) {
        final var sender = context.getSource().getSender();
        final var locale = sender instanceof final Player p ? p.locale() : Locale.US;
        if (!players.isEmpty()) players.forEach(player -> (world != null
                ? plugin.economyController().tryGetAccount(player, world)
                : plugin.economyController().tryGetAccount(player))
                .thenAccept(optional -> optional.ifPresentOrElse(account -> {
                    final var balance = function.apply(account, amount);
                    plugin.bundle().sendMessage(sender, successMessage,
                            Placeholder.parsed("world", world != null ? world.getName() : "null"),
                            Placeholder.parsed("player", String.valueOf(player.getName())),
                            Placeholder.parsed("balance", plugin.economyController().format(balance, locale)),
                            Placeholder.parsed("amount", plugin.economyController().format(amount, locale)),
                            Placeholder.parsed("symbol", plugin.economyController().getCurrencySymbol()));
                }, () -> plugin.bundle().sendMessage(sender, world != null
                                ? (sender.equals(player) ? "account.not-found.world.self" : "account.not-found.world.other")
                                : (sender.equals(player) ? "account.not-found.self" : "account.not-found.other"),
                        Placeholder.parsed("player", String.valueOf(player.getName())),
                        Placeholder.parsed("world", world != null ? world.getName() : "null")))));
        else plugin.bundle().sendMessage(sender, "player.define");
        return Command.SINGLE_SUCCESS;
    }
}
