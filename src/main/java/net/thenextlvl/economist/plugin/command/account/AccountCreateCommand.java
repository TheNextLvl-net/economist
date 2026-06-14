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
import net.thenextlvl.economist.Account;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class AccountCreateCommand extends SimpleCommand {
    private AccountCreateCommand(final EconomistPlugin plugin) {
        super(plugin, "create", "economist.account.create");
    }

    public static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new AccountCreateCommand(plugin);
        final var player = Commands.argument("player", OfflinePlayerArgumentType.player())
                .requires(stack -> stack.getSender().hasPermission("economist.account.create.others"));
        final var players = Commands.argument("players", ArgumentTypes.players())
                .requires(stack -> stack.getSender().hasPermission("economist.account.create.others"));
        final var world = Commands.argument("world", ArgumentTypes.world())
                .requires(stack -> stack.getSender().hasPermission("economist.account.create.world")
                        && plugin.config.accounts.perWorld);
        return command.create()
                .then(player.executes(command).then(world.executes(command)))
                .then(players.executes(command).then(world.executes(command)))
                .executes(command);
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var sender = context.getSource().getSender();

        final var players = resolveArgument(context, "players", PlayerSelectorArgumentResolver.class)
                .map(List::<OfflinePlayer>copyOf)
                .orElseGet(() -> tryGetArgument(context, "player", OfflinePlayer.class)
                        .map(List::of)
                        .orElseGet(() -> sender instanceof final Player player ? List.of(player) : List.of()));
        final var world = tryGetArgument(context, "world", World.class).orElse(null);

        if (players.isEmpty()) {
            plugin.bundle().sendMessage(sender, "player.define");
            return 0;
        }

        players.forEach(player -> createAccount(player, world).thenAccept(account -> {
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
        return SINGLE_SUCCESS;
    }

    private CompletableFuture<Account> createAccount(final OfflinePlayer player, @Nullable final World world) {
        if (world == null) return plugin.economyController().createAccount(player);
        return plugin.economyController().createAccount(player, world);
    }
}
