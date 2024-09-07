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
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.Account;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class PayCommand {
    private final EconomistPlugin plugin;

    public void register() {
        var amountArgument = DoubleArgumentType.doubleArg(plugin.config().minimumPayment());
        var command = Commands.literal("pay")
                .requires(stack -> stack.getSender() instanceof Player player && player.hasPermission("economist.pay"))
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
                            return pay(context, new ArrayList<>(players.resolve(context.getSource())), null);
                        }).then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> stack.getSender().hasPermission("economist.pay.world"))
                                .executes(context -> {
                                    var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
                                    var world = context.getArgument("world", World.class);
                                    return pay(context, new ArrayList<>(players.resolve(context.getSource())), world);
                                }))))
                .build();
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event ->
                event.registrar().register(command, "Pay another player")));
    }

    private int pay(CommandContext<CommandSourceStack> context, List<? extends OfflinePlayer> players, @Nullable World world) {
        var sender = (Player) context.getSource().getSender();

        var amount = context.getArgument("amount", Double.class);
        var minimum = sender.hasPermission("economist.loan") ? -plugin.config().maxLoanAmount() : 0;

        if (players.isEmpty()) {
            plugin.bundle().sendMessage(sender, "player.define");
            return 0;
        } else if (players.size() == 1 && players.getFirst().equals(sender)) {
            plugin.bundle().sendMessage(sender, "player.pay.self");
            return 0;
        } else if (players.size() > 1) players.remove(sender);

        getAccount(sender, world).thenAccept(optional -> optional.ifPresentOrElse(account ->
                        players.forEach(player -> getAccount(player, world).thenAccept(optional1 ->
                                optional1.ifPresentOrElse(target ->
                                                pay(sender, player, account, target, amount, minimum),
                                        () -> missingAccount(world, sender, player)
                                ))),
                () -> missingAccount(world, sender, sender)));

        return players.size();
    }

    private CompletableFuture<Optional<Account>> getAccount(OfflinePlayer player, @Nullable World world) {
        if (world == null) return plugin.economyController().tryGetAccount(player);
        return plugin.economyController().tryGetAccount(player, world);
    }

    private void pay(Player sender, OfflinePlayer player, Account source, Account target, double amount, double minimum) {
        if (source.getBalance().doubleValue() - amount < minimum) {
            plugin.bundle().sendMessage(sender, "account.funds");
            return;
        }

        source.withdraw(amount);
        target.deposit(amount);

        plugin.bundle().sendMessage(sender, "player.pay.outgoing",
                Placeholder.parsed("amount", plugin.economyController().format(amount, sender.locale())),
                Placeholder.parsed("player", player.getName() != null ? player.getName() : "null"),
                Placeholder.parsed("symbol", plugin.economyController().getCurrencySymbol()));

        var online = player.getPlayer();
        if (online != null) plugin.bundle().sendMessage(online, "player.pay.incoming",
                Placeholder.parsed("amount", plugin.economyController().format(amount, online.locale())),
                Placeholder.parsed("symbol", plugin.economyController().getCurrencySymbol()),
                Placeholder.parsed("player", sender.getName()));
    }

    private void missingAccount(@Nullable World world, Player sender, OfflinePlayer player) {
        plugin.bundle().sendMessage(sender, world != null
                        ? (player.equals(sender) ? "account.not-found.world.self" : "account.not-found.world.other")
                        : (player.equals(sender) ? "account.not-found.self" : "account.not-found.other"),
                Placeholder.parsed("player", player.getName() != null ? player.getName() : "null"),
                Placeholder.parsed("world", world != null ? world.key().asString() : "null"));
    }
}
