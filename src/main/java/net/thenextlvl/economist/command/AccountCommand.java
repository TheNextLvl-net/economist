package net.thenextlvl.economist.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
import net.thenextlvl.economist.command.argument.DurationArgument;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class AccountCommand {
    public static LiteralCommandNode<CommandSourceStack> create(final EconomistPlugin plugin) {
        return Commands.literal("account")
                .requires(stack -> stack.getSender().hasPermission("economist.account"))
                .then(BalanceCommand.create(plugin))
                .then(createArgument(plugin))
                .then(deleteArgument(plugin))
                .then(pruneArgument(plugin))
                .build();
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createArgument(final EconomistPlugin plugin) {
        return Commands.literal("create")
                .requires(stack -> stack.getSender().hasPermission("economist.account.create"))
                .then(Commands.argument("player", OfflinePlayerArgumentType.player())
                        .requires(stack -> stack.getSender().hasPermission("economist.account.create.others"))
                        .then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> stack.getSender().hasPermission("economist.account.create.world") && plugin.config.accounts.perWorld)
                                .executes(context -> {
                                    final var player = context.getArgument("player", OfflinePlayer.class);
                                    final var world = context.getArgument("world", World.class);
                                    return create(context, List.of(player), world, plugin);
                                }))
                        .executes(context -> {
                            final var player = context.getArgument("player", OfflinePlayer.class);
                            return create(context, List.of(player), null, plugin);
                        }))
                .then(Commands.argument("players", ArgumentTypes.players())
                        .requires(stack -> stack.getSender().hasPermission("economist.account.create.others"))
                        .then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> stack.getSender().hasPermission("economist.account.create.world") && plugin.config.accounts.perWorld)
                                .executes(context -> {
                                    final var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                                    final var world = context.getArgument("world", World.class);
                                    final var resolve = players.resolve(context.getSource());
                                    return create(context, resolve, world, plugin);
                                }))
                        .executes(context -> {
                            final var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                            final var resolve = players.resolve(context.getSource());
                            return create(context, resolve, null, plugin);
                        }))
                .executes(context -> {
                    final var players = context.getSource().getSender() instanceof final Player player
                            ? List.<OfflinePlayer>of(player) : List.<OfflinePlayer>of();
                    return create(context, players, null, plugin);
                });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> deleteArgument(final EconomistPlugin plugin) {
        return Commands.literal("delete")
                .requires(stack -> stack.getSender().hasPermission("economist.account.delete"))
                .then(Commands.argument("player", OfflinePlayerArgumentType.player())
                        .requires(stack -> stack.getSender().hasPermission("economist.account.delete.others"))
                        .then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> stack.getSender().hasPermission("economist.account.delete.world") && plugin.config.accounts.perWorld)
                                .executes(context -> {
                                    final var player = context.getArgument("player", OfflinePlayer.class);
                                    final var world = context.getArgument("world", World.class);
                                    return delete(context, List.of(player), world, plugin);
                                }))
                        .executes(context -> {
                            final var player = context.getArgument("player", OfflinePlayer.class);
                            return delete(context, List.of(player), null, plugin);
                        }))
                .then(Commands.argument("players", ArgumentTypes.players())
                        .requires(stack -> stack.getSender().hasPermission("economist.account.delete.others"))
                        .then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> stack.getSender().hasPermission("economist.account.delete.world") && plugin.config.accounts.perWorld)
                                .executes(context -> {
                                    final var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                                    final var world = context.getArgument("world", World.class);
                                    final var resolve = players.resolve(context.getSource());
                                    return delete(context, resolve, world, plugin);
                                }))
                        .executes(context -> {
                            final var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                            final var resolve = players.resolve(context.getSource());
                            return delete(context, resolve, null, plugin);
                        }))
                .executes(context -> {
                    final var players = context.getSource().getSender() instanceof final Player player
                            ? List.<OfflinePlayer>of(player) : List.<OfflinePlayer>of();
                    return delete(context, players, null, plugin);
                });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> pruneArgument(final EconomistPlugin plugin) {
        final var min = Duration.ofDays(plugin.config.minimumPruneDays);
        return Commands.literal("prune")
                .requires(stack -> stack.getSender().hasPermission("economist.account.prune"))
                .then(Commands.argument("time", DurationArgument.duration(min))
                        .then(Commands.argument("world", ArgumentTypes.world())
                                .executes(context -> prune(context, context.getArgument("world", World.class), plugin)))
                        .executes(context -> prune(context, null, plugin)));
    }

    private static int prune(final CommandContext<CommandSourceStack> context, @Nullable final World world, final EconomistPlugin plugin) {
        final var duration = context.getArgument("time", Duration.class);
        CompletableFuture.supplyAsync(() -> {
                    try {
                        return plugin.dataController().getAccountOwners(world);
                    } catch (final SQLException e) {
                        plugin.getComponentLogger().error("Failed to load account owners", e);
                        return new HashSet<UUID>();
                    }
                })
                .thenApply(accounts -> accounts.stream().map(plugin.getServer()::getOfflinePlayer))
                .thenApply(players -> players.filter(player -> !player.isConnected())
                        .filter(player -> player.getLastSeen() < Instant.now().minus(duration).toEpochMilli()))
                .thenAcceptAsync(players -> prune(context, players.toList(), world, plugin));
        return Command.SINGLE_SUCCESS;
    }

    private static void prune(final CommandContext<CommandSourceStack> context, final List<OfflinePlayer> players, @Nullable final World world, final EconomistPlugin plugin) {
        final var sender = context.getSource().getSender();
        final var placeholder = Placeholder.parsed("world", world != null ? world.getName() : "null");
        deleteAccounts(players.stream().map(OfflinePlayer::getUniqueId).toList(), world, plugin).thenAccept(success -> {
            final var message = players.isEmpty()
                    ? (world != null ? "account.prune.none.world" : "account.prune.none")
                    : (world != null ? "account.prune.success.world" : "account.prune.success");
            plugin.bundle().sendMessage(sender, message, placeholder,
                    Placeholder.parsed("pruned", String.valueOf(players.size())));
        });
    }

    private static int create(final CommandContext<CommandSourceStack> context, final Collection<? extends OfflinePlayer> players, @Nullable final World world, final EconomistPlugin plugin) {
        final var sender = context.getSource().getSender();
        if (players.isEmpty()) plugin.bundle().sendMessage(sender, "player.define");
        else players.forEach(player -> createAccount(player, world, plugin).thenAccept(account -> {
            final var message = world != null
                    ? (player.equals(sender) ? "account.created.world.self" : "account.created.world.other")
                    : (player.equals(sender) ? "account.created.self" : "account.created.other");
            plugin.bundle().sendMessage(sender, message,
                    Placeholder.parsed("player", player.getName() != null ? player.getName() : player.getUniqueId().toString()),
                    Placeholder.parsed("world", world != null ? world.getName() : "null"));
        }).exceptionally(throwable -> {
            final var message = world != null
                    ? (player.equals(sender) ? "account.exists.world.self" : "account.exists.world.other")
                    : (player.equals(sender) ? "account.exists.self" : "account.exists.other");
            plugin.bundle().sendMessage(sender, message,
                    Placeholder.parsed("player", player.getName() != null ? player.getName() : player.getUniqueId().toString()),
                    Placeholder.parsed("world", world != null ? world.getName() : "null"));
            return null;
        }));
        return Command.SINGLE_SUCCESS;
    }

    private static int delete(final CommandContext<CommandSourceStack> context, final Collection<? extends OfflinePlayer> players, @Nullable final World world, final EconomistPlugin plugin) {
        final var sender = context.getSource().getSender();
        if (players.isEmpty()) plugin.bundle().sendMessage(sender, "player.define");
        else players.forEach(player -> deleteAccount(player, world, plugin).thenAccept(success -> {
            final var message = success ? (world != null
                    ? (player.equals(sender) ? "account.deleted.world.self" : "account.deleted.world.other")
                    : (player.equals(sender) ? "account.deleted.self" : "account.deleted.other"))
                    : (world != null
                    ? (player.equals(sender) ? "account.not-found.world.self" : "account.not-found.world.other")
                    : (player.equals(sender) ? "account.not-found.self" : "account.not-found.other"));
            plugin.bundle().sendMessage(sender, message,
                    Placeholder.parsed("player", player.getName() != null ? player.getName() : player.getUniqueId().toString()),
                    Placeholder.parsed("world", world != null ? world.getName() : "null"));
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().error("Failed to delete account for {}", player.getName(), throwable);
            return null;
        }));
        return Command.SINGLE_SUCCESS;
    }

    private static CompletableFuture<Account> createAccount(final OfflinePlayer player, @Nullable final World world, final EconomistPlugin plugin) {
        if (world == null) return plugin.economyController().createAccount(player);
        return plugin.economyController().createAccount(player, world);
    }

    private static CompletableFuture<Boolean> deleteAccount(final OfflinePlayer player, @Nullable final World world, final EconomistPlugin plugin) {
        if (world == null) return plugin.economyController().deleteAccount(player);
        return plugin.economyController().deleteAccount(player, world);
    }

    private static CompletableFuture<Boolean> deleteAccounts(final List<UUID> accounts, @Nullable final World world, final EconomistPlugin plugin) {
        if (world == null) return plugin.economyController().deleteAccounts(accounts);
        return plugin.economyController().deleteAccounts(accounts, world);
    }
}
