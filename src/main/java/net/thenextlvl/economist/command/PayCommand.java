package net.thenextlvl.economist.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import core.paper.command.CustomArgumentTypes;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.Account;
import net.thenextlvl.economist.api.currency.Currency;
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
    public static LiteralCommandNode<CommandSourceStack> create(EconomistPlugin plugin) {
        var amountArgument = DoubleArgumentType.doubleArg(plugin.config.minimumPayment);
        return Commands.literal("pay")
                .requires(stack -> stack.getSender() instanceof Player player && player.hasPermission("economist.pay"))
                .then(Commands.argument("player", CustomArgumentTypes.cachedOfflinePlayer())
                        .then(Commands.argument("amount", amountArgument).executes(context -> {
                            var player = context.getArgument("player", OfflinePlayer.class);
                            return pay(context, List.of(player), null, plugin);
                        }).then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> stack.getSender().hasPermission("economist.pay.world") && plugin.config.accounts.perWorld)
                                .executes(context -> {
                                    var player = context.getArgument("player", OfflinePlayer.class);
                                    var world = context.getArgument("world", World.class);
                                    return pay(context, List.of(player), world, plugin);
                                }))))
                .then(Commands.argument("players", ArgumentTypes.players())
                        .then(Commands.argument("amount", amountArgument).executes(context -> {
                            var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                            return pay(context, new ArrayList<>(players.resolve(context.getSource())), null, plugin);
                        }).then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> stack.getSender().hasPermission("economist.pay.world") && plugin.config.accounts.perWorld)
                                .executes(context -> {
                                    var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                                    var world = context.getArgument("world", World.class);
                                    return pay(context, new ArrayList<>(players.resolve(context.getSource())), world, plugin);
                                }))))
                .build();
    }

    private static int pay(CommandContext<CommandSourceStack> context, List<? extends OfflinePlayer> players, @Nullable World world, EconomistPlugin plugin) {
        var sender = (Player) context.getSource().getSender();

        var amount = context.getArgument("amount", Double.class);
        var minimum = sender.hasPermission("economist.loan") ? -plugin.config.maxLoanAmount : 0;

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

    private static CompletableFuture<Optional<Account>> getAccount(OfflinePlayer player, @Nullable World world, EconomistPlugin plugin) {
        if (world == null) return plugin.economyController().tryGetAccount(player);
        return plugin.economyController().tryGetAccount(player, world);
    }

    private static void pay(Player sender, OfflinePlayer player, Account source, Account target, double amount, double minimum, EconomistPlugin plugin) {
        Currency currency = null; // fixme

        if (source.getBalance(currency).doubleValue() - amount < minimum) {
            plugin.bundle().sendMessage(sender, "account.funds");
            return;
        }

        source.withdraw(amount, currency);
        target.deposit(amount, currency);

        plugin.bundle().sendMessage(sender, "player.pay.outgoing",
                Placeholder.component("amount", currency.format(amount, sender.locale())),
                Placeholder.component("symbol", currency.getSymbol()),
                Placeholder.parsed("player", player.getName() != null ? player.getName() : player.getUniqueId().toString()));

        var online = player.getPlayer();
        if (online != null) plugin.bundle().sendMessage(online, "player.pay.incoming",
                Placeholder.component("amount", currency.format(amount, online.locale())),
                Placeholder.component("symbol", currency.getSymbol()),
                Placeholder.parsed("player", sender.getName()));
    }

    private static void missingAccount(@Nullable World world, Player sender, OfflinePlayer player, EconomistPlugin plugin) {
        plugin.bundle().sendMessage(sender, world != null
                        ? (player.equals(sender) ? "account.not-found.world.self" : "account.not-found.world.other")
                        : (player.equals(sender) ? "account.not-found.self" : "account.not-found.other"),
                Placeholder.parsed("player", player.getName() != null ? player.getName() : player.getUniqueId().toString()),
                Placeholder.parsed("world", world != null ? world.getName() : "null"));
    }
}
