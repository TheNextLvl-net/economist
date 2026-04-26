package net.thenextlvl.economist.plugin.command.account;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.argument.DurationArgument;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

final class AccountPruneCommand extends SimpleCommand {
    private AccountPruneCommand(final EconomistPlugin plugin) {
        super(plugin, "prune", "economist.account.prune");
    }

    public static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new AccountPruneCommand(plugin);
        final var min = Duration.ofDays(plugin.config.minimumPruneDays);
        final var time = Commands.argument("time", DurationArgument.duration(min));
        final var world = Commands.argument("world", ArgumentTypes.world())
                .requires(stack -> plugin.config.accounts.perWorld);
        return command.create().then(time.executes(command).then(world.executes(command)));
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var world = tryGetArgument(context, "world", World.class).orElse(null);
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
                .thenAcceptAsync(players -> prune(context, players.toList(), world));
        return SINGLE_SUCCESS;
    }

    private void prune(final CommandContext<CommandSourceStack> context, final List<OfflinePlayer> players,
                       @Nullable final World world) {
        final var sender = context.getSource().getSender();
        final var placeholder = Placeholder.parsed("world", world != null ? world.getName() : "null");
        deleteAccounts(players.stream().map(OfflinePlayer::getUniqueId).toList(), world).thenAccept(success -> {
            final var message = players.isEmpty()
                    ? (world != null ? "account.prune.none.world" : "account.prune.none")
                    : (world != null ? "account.prune.success.world" : "account.prune.success");
            plugin.bundle().sendMessage(sender, message, placeholder,
                    Placeholder.parsed("pruned", String.valueOf(players.size())));
        });
    }

    private CompletableFuture<Boolean> deleteAccounts(final List<UUID> accounts, @Nullable final World world) {
        if (world == null) return plugin.economyController().deleteAccounts(accounts);
        return plugin.economyController().deleteAccounts(accounts, world);
    }
}
