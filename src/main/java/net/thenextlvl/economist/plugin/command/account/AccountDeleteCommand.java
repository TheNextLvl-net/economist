package net.thenextlvl.economist.plugin.command.account;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import core.paper.brigadier.arguments.OfflinePlayerArgumentType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class AccountDeleteCommand extends SimpleCommand {
    private AccountDeleteCommand(final EconomistPlugin plugin) {
        super(plugin, "delete", "economist.account.delete");
    }

    public static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new AccountDeleteCommand(plugin);
        final var player = Commands.argument("player", OfflinePlayerArgumentType.player());
        final var players = Commands.argument("players", ArgumentTypes.players());
        final var world = Commands.argument("world", ArgumentTypes.world())
                .requires(stack -> stack.getSender().hasPermission("economist.account.delete.world")
                        && plugin.config.accounts.perWorld);
        return command.create()
                .then(player.executes(command).then(world.executes(command)))
                .then(players.executes(command).then(world.executes(command)));
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var sender = context.getSource().getSender();

        final var players = resolveArgument(context, "players", PlayerSelectorArgumentResolver.class)
                .map(List::<OfflinePlayer>copyOf)
                .orElseGet(() -> tryGetArgument(context, "player", OfflinePlayer.class)
                        .map(List::of).orElseGet(List::of));
        final var world = tryGetArgument(context, "world", World.class).orElse(null);

        if (players.isEmpty()) {
            plugin.bundle().sendMessage(sender, "player.define");
            return 0;
        }

        players.forEach(player -> deleteAccount(player, world).thenAccept(success -> {
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
            plugin.bundle().sendMessage(sender, "operation.failed");
            plugin.getComponentLogger().error("Failed to delete account for {}", player.getName(), throwable);
            return null;
        }));
        return SINGLE_SUCCESS;
    }

    private CompletableFuture<Boolean> deleteAccount(final OfflinePlayer player, @Nullable final World world) {
        if (world == null) return plugin.economyController().deleteAccount(player);
        return plugin.economyController().deleteAccount(player, world);
    }
}
