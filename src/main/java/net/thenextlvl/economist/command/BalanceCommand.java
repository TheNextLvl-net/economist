package net.thenextlvl.economist.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import core.paper.command.CustomArgumentTypes;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.currency.Currency;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class BalanceCommand {
    public static LiteralCommandNode<CommandSourceStack> create(EconomistPlugin plugin) {
        return Commands.literal("balance")
                .requires(stack -> stack.getSender().hasPermission("economist.balance"))
                .then(Commands.argument("player", CustomArgumentTypes.cachedOfflinePlayer())
                        .requires(stack -> stack.getSender().hasPermission("economist.balance.others"))
                        .then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> stack.getSender().hasPermission("economist.balance.world") && plugin.config.accounts.perWorld)
                                .executes(context -> {
                                    var player = context.getArgument("player", OfflinePlayer.class);
                                    var world = context.getArgument("world", World.class);
                                    return balance(context, player, world, plugin);
                                }))
                        .executes(context -> {
                            var player = context.getArgument("player", OfflinePlayer.class);
                            return balance(context, player, null, plugin);
                        }))
                .executes(context -> {
                    var sender = context.getSource().getSender();
                    if (sender instanceof Player player) return balance(context, player, null, plugin);
                    plugin.bundle().sendMessage(sender, "player.define");
                    return 0;
                })
                .build();
    }

    private static int balance(CommandContext<CommandSourceStack> context, OfflinePlayer player, @Nullable World world, EconomistPlugin plugin) {
        var sender = context.getSource().getSender();
        var controller = plugin.economyController();
        Currency currency = null; // fixme
        controller.tryGetAccount(player, world).thenAccept(optional -> optional.ifPresentOrElse(account -> {
            var message = world != null
                    ? (player.equals(sender) ? "account.balance.world.self" : "account.balance.world.other")
                    : (player.equals(sender) ? "account.balance.self" : "account.balance.other");

            plugin.bundle().sendMessage(sender, message,
                    Placeholder.parsed("player", String.valueOf(player.getName())),
                    Placeholder.component("balance", currency.format(account.getBalance(currency), sender)),
                    Placeholder.component("currency", account.getBalance(currency).intValue() == 1
                            ? currency.getDisplayNameSingular(sender).orElse(Component.empty())
                            : currency.getDisplayNamePlural(sender).orElse(Component.empty())),
                    Placeholder.component("symbol", currency.getSymbol()),
                    Placeholder.parsed("world", world != null ? world.getName() : "null"));

        }, () -> plugin.bundle().sendMessage(sender, world != null
                        ? (player.equals(sender) ? "account.not-found.world.self" : "account.not-found.world.other")
                        : (player.equals(sender) ? "account.not-found.self" : "account.not-found.other"),
                Placeholder.parsed("player", String.valueOf(player.getName())),
                Placeholder.parsed("world", world != null ? world.getName() : "null"))));

        return Command.SINGLE_SUCCESS;
    }
}
