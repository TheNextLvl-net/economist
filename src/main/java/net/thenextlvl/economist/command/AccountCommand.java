package net.thenextlvl.economist.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import core.paper.command.CustomArgumentTypes;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.Account;
import net.thenextlvl.economist.command.argument.DurationArgument;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class AccountCommand {
    private final EconomistPlugin plugin;

    public void register() {
        var command = Commands.literal("account")
                .requires(stack -> stack.getSender().hasPermission("economist.account"))
                .then(new BalanceCommand(plugin).create())
                .then(create())
                .then(delete())
                .then(prune())
                .build();
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event ->
                event.registrar().register(command, "Manage user accounts")));
    }

    private LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("create")
                .requires(stack -> stack.getSender().hasPermission("economist.account.create"))
                .then(Commands.argument("player", CustomArgumentTypes.cachedOfflinePlayer())
                        .requires(stack -> stack.getSender().hasPermission("economist.account.create.others"))
                        .then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> stack.getSender().hasPermission("economist.account.create.world"))
                                .executes(context -> {
                                    var player = context.getArgument("player", OfflinePlayer.class);
                                    var world = context.getArgument("world", World.class);
                                    return create(context, List.of(player), world);
                                }))
                        .executes(context -> {
                            var player = context.getArgument("player", OfflinePlayer.class);
                            return create(context, List.of(player), null);
                        }))
                .then(Commands.argument("players", ArgumentTypes.players())
                        .requires(stack -> stack.getSender().hasPermission("economist.account.create.others"))
                        .then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> stack.getSender().hasPermission("economist.account.create.world"))
                                .executes(context -> {
                                    var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                                    var world = context.getArgument("world", World.class);
                                    var resolve = players.resolve(context.getSource());
                                    return create(context, resolve, world);
                                }))
                        .executes(context -> {
                            var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                            var resolve = players.resolve(context.getSource());
                            return create(context, resolve, null);
                        }))
                .executes(context -> {
                    var players = context.getSource().getSender() instanceof Player player
                            ? List.<OfflinePlayer>of(player) : List.<OfflinePlayer>of();
                    return create(context, players, null);
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> delete() {
        return Commands.literal("delete")
                .requires(stack -> stack.getSender().hasPermission("economist.account.delete"))
                .then(Commands.argument("player", CustomArgumentTypes.cachedOfflinePlayer())
                        .requires(stack -> stack.getSender().hasPermission("economist.account.delete.others"))
                        .then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> stack.getSender().hasPermission("economist.account.delete.world"))
                                .executes(context -> {
                                    var player = context.getArgument("player", OfflinePlayer.class);
                                    var world = context.getArgument("world", World.class);
                                    return delete(context, List.of(player), world);
                                }))
                        .executes(context -> {
                            var player = context.getArgument("player", OfflinePlayer.class);
                            return delete(context, List.of(player), null);
                        }))
                .then(Commands.argument("players", ArgumentTypes.players())
                        .requires(stack -> stack.getSender().hasPermission("economist.account.delete.others"))
                        .then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> stack.getSender().hasPermission("economist.account.delete.world"))
                                .executes(context -> {
                                    var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                                    var world = context.getArgument("world", World.class);
                                    var resolve = players.resolve(context.getSource());
                                    return delete(context, resolve, world);
                                }))
                        .executes(context -> {
                            var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                            var resolve = players.resolve(context.getSource());
                            return delete(context, resolve, null);
                        }))
                .executes(context -> {
                    var players = context.getSource().getSender() instanceof Player player
                            ? List.<OfflinePlayer>of(player) : List.<OfflinePlayer>of();
                    return delete(context, players, null);
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> prune() {
        var min = Duration.ofDays(plugin.config().minimumPruneDays());
        return Commands.literal("prune")
                .requires(stack -> stack.getSender().hasPermission("economist.account.prune"))
                .then(Commands.argument("time", DurationArgument.duration(min))
                        .then(Commands.argument("world", ArgumentTypes.world())
                                .executes(context -> prune(context, context.getArgument("world", World.class))))
                        .executes(context -> prune(context, null)));
    }

    private int prune(CommandContext<CommandSourceStack> context, @Nullable World world) {
        var duration = context.getArgument("time", Duration.class);
        CompletableFuture.supplyAsync(() -> plugin.dataController().getAccounts(world))
                .thenApply(accounts -> accounts.stream().map(plugin.getServer()::getOfflinePlayer))
                .thenApply(players -> players.filter(player -> !player.isConnected())
                        .filter(player -> player.getLastSeen() < Instant.now().minus(duration).toEpochMilli()))
                .thenAcceptAsync(players -> prune(context, players.toList(), world));
        return Command.SINGLE_SUCCESS;
    }

    @SneakyThrows
    private void prune(CommandContext<CommandSourceStack> context, List<OfflinePlayer> players, @Nullable World world) {
        var sender = context.getSource().getSender();
        var placeholder = Placeholder.parsed("world", world != null ? world.key().asString() : "null");
        var latch = new CountDownLatch(players.size());
        players.forEach(player -> deleteAccount(player, world).thenAcceptAsync(success -> {
            var message = world != null ? "account.prune.player.world" : "account.prune.player";
            var identity = player.getName() != null ? player.getName() : player.getUniqueId().toString();
            plugin.bundle().sendMessage(sender, message, placeholder, Placeholder.parsed("player", identity));
            latch.countDown();
        }));
        latch.await();
        var message = players.isEmpty()
                ? (world != null ? "account.prune.none.world" : "account.prune.none")
                : (world != null ? "account.prune.success.world" : "account.prune.success");
        plugin.bundle().sendMessage(sender, message, placeholder,
                Placeholder.parsed("pruned", String.valueOf(players.size())));
    }

    private int create(CommandContext<CommandSourceStack> context, Collection<? extends OfflinePlayer> players, @Nullable World world) {
        var sender = context.getSource().getSender();
        if (players.isEmpty()) plugin.bundle().sendMessage(sender, "player.define");
        else players.forEach(player -> createAccount(player, world).thenAccept(account -> {
            var message = world != null
                    ? (player.equals(sender) ? "account.created.world.self" : "account.created.world.other")
                    : (player.equals(sender) ? "account.created.self" : "account.created.other");
            plugin.bundle().sendMessage(sender, message,
                    Placeholder.parsed("player", player.getName() != null ? player.getName() : player.getUniqueId().toString()),
                    Placeholder.parsed("world", world != null ? world.key().asString() : "null"));
        }).exceptionally(throwable -> {
            var message = world != null
                    ? (player.equals(sender) ? "account.exists.world.self" : "account.exists.world.other")
                    : (player.equals(sender) ? "account.exists.self" : "account.exists.other");
            plugin.bundle().sendMessage(sender, message,
                    Placeholder.parsed("player", player.getName() != null ? player.getName() : player.getUniqueId().toString()),
                    Placeholder.parsed("world", world != null ? world.key().asString() : "null"));
            return null;
        }));
        return Command.SINGLE_SUCCESS;
    }

    private int delete(CommandContext<CommandSourceStack> context, Collection<? extends OfflinePlayer> players, @Nullable World world) {
        var sender = context.getSource().getSender();
        if (players.isEmpty()) plugin.bundle().sendMessage(sender, "player.define");
        else players.forEach(player -> deleteAccount(player, world).thenAccept(success -> {
            var message = success ? (world != null
                    ? (player.equals(sender) ? "account.deleted.world.self" : "account.deleted.world.other")
                    : (player.equals(sender) ? "account.deleted.self" : "account.deleted.other"))
                    : (world != null
                    ? (player.equals(sender) ? "account.not-found.world.self" : "account.not-found.world.other")
                    : (player.equals(sender) ? "account.not-found.self" : "account.not-found.other"));
            plugin.bundle().sendMessage(sender, message,
                    Placeholder.parsed("player", player.getName() != null ? player.getName() : player.getUniqueId().toString()),
                    Placeholder.parsed("world", world != null ? world.key().asString() : "null"));
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().error("Failed to delete account for {}", player.getName(), throwable);
            return null;
        }));
        return Command.SINGLE_SUCCESS;
    }

    private CompletableFuture<Account> createAccount(OfflinePlayer player, @Nullable World world) {
        if (world == null) return plugin.economyController().createAccount(player);
        return plugin.economyController().createAccount(player, world);
    }

    private CompletableFuture<Boolean> deleteAccount(OfflinePlayer player, @Nullable World world) {
        if (world == null) return plugin.economyController().deleteAccount(player);
        return plugin.economyController().deleteAccount(player, world);
    }
}
