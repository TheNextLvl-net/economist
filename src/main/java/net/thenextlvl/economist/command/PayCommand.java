package net.thenextlvl.economist.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class PayCommand {
    public static LiteralCommandNode<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var amountArgument = DoubleArgumentType.doubleArg(plugin.config.minimumPayment);
        return Commands.literal("pay")
                .requires(stack -> stack.getSender() instanceof final Player player && player.hasPermission("economist.pay"))
                .then(Commands.argument("player", OfflinePlayerArgumentType.player())
                        .then(Commands.argument("amount", amountArgument).executes(context -> {
                            final var player = context.getArgument("player", OfflinePlayer.class);
                            return pay(context, List.of(player), null, plugin);
                        }).then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> stack.getSender().hasPermission("economist.pay.world") && plugin.config.accounts.perWorld)
                                .executes(context -> {
                                    final var player = context.getArgument("player", OfflinePlayer.class);
                                    final var world = context.getArgument("world", World.class);
                                    return pay(context, List.of(player), world, plugin);
                                }))))
                .then(Commands.argument("players", ArgumentTypes.players())
                        .then(Commands.argument("amount", amountArgument).executes(context -> {
                            final var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                            return pay(context, new ArrayList<>(players.resolve(context.getSource())), null, plugin);
                        }).then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> stack.getSender().hasPermission("economist.pay.world") && plugin.config.accounts.perWorld)
                                .executes(context -> {
                                    final var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                                    final var world = context.getArgument("world", World.class);
                                    return pay(context, new ArrayList<>(players.resolve(context.getSource())), world, plugin);
                                }))))
                .build();
    }

    private static int pay(final CommandContext<CommandSourceStack> context, final List<? extends OfflinePlayer> players, @Nullable final World world, final EconomistPlugin plugin) {
        final var sender = (Player) context.getSource().getSender();

        final var amount = context.getArgument("amount", Double.class);
        final var minimum = sender.hasPermission("economist.loan") ? -plugin.config.maxLoanAmount : 0;

        if (players.isEmpty()) {
            plugin.bundle().sendMessage(sender, "player.define");
            return 0;
        } else if (players.size() == 1 && players.getFirst().equals(sender)) {
            plugin.bundle().sendMessage(sender, "player.pay.self");
            return 0;
        } else if (players.size() > 1) players.remove(sender);

        getAccount(sender, world, plugin).thenAccept(optional -> optional.ifPresentOrElse(account ->
                        players.forEach(player -> getAccount(player, world, plugin).thenAccept(optional1 ->
                                optional1.ifPresentOrElse(target ->
                                                pay(sender, player, account, target, amount, minimum, plugin),
                                        () -> missingAccount(world, sender, player, plugin)
                                ))),
                () -> missingAccount(world, sender, sender, plugin)));

        return players.size();
    }

    private static CompletableFuture<Optional<Account>> getAccount(final OfflinePlayer player, @Nullable final World world, final EconomistPlugin plugin) {
        if (world == null) return plugin.economyController().tryGetAccount(player);
        return plugin.economyController().tryGetAccount(player, world);
    }

    private static void pay(final Player sender, final OfflinePlayer player, final Account source, final Account target, final double amount, final double minimum, final EconomistPlugin plugin) {
        if (source.getBalance().doubleValue() - amount < minimum) {
            plugin.bundle().sendMessage(sender, "account.funds");
            return;
        }

        source.withdraw(amount);
        target.deposit(amount);

        plugin.bundle().sendMessage(sender, "player.pay.outgoing",
                Placeholder.parsed("amount", plugin.economyController().format(amount, sender.locale())),
                Placeholder.parsed("player", player.getName() != null ? player.getName() : player.getUniqueId().toString()),
                Placeholder.parsed("symbol", plugin.economyController().getCurrencySymbol()));

        final var online = player.getPlayer();
        if (online != null) plugin.bundle().sendMessage(online, "player.pay.incoming",
                Placeholder.parsed("amount", plugin.economyController().format(amount, online.locale())),
                Placeholder.parsed("symbol", plugin.economyController().getCurrencySymbol()),
                Placeholder.parsed("player", sender.getName()));
    }

    private static void missingAccount(@Nullable final World world, final Player sender, final OfflinePlayer player, final EconomistPlugin plugin) {
        plugin.bundle().sendMessage(sender, world != null
                        ? (player.equals(sender) ? "account.not-found.world.self" : "account.not-found.world.other")
                        : (player.equals(sender) ? "account.not-found.self" : "account.not-found.other"),
                Placeholder.parsed("player", player.getName() != null ? player.getName() : player.getUniqueId().toString()),
                Placeholder.parsed("world", world != null ? world.getName() : "null"));
    }
}
