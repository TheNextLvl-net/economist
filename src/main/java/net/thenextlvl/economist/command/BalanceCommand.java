package net.thenextlvl.economist.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import core.paper.brigadier.arguments.OfflinePlayerArgumentType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.EconomistPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

@NullMarked
public class BalanceCommand {
    public static LiteralCommandNode<CommandSourceStack> create(EconomistPlugin plugin) {
        return Commands.literal("balance")
                .requires(stack -> stack.getSender().hasPermission("economist.balance"))
                .then(Commands.argument("player", OfflinePlayerArgumentType.player())
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
        Optional.ofNullable(world)
                .map(w -> controller.tryGetAccount(player, w))
                .orElseGet(() -> controller.tryGetAccount(player))
                .thenAccept(optional -> optional.ifPresentOrElse(account -> {
                    var locale = sender instanceof Player p ? p.locale() : Locale.US;

                    var message = world != null
                            ? (player.equals(sender) ? "account.balance.world.self" : "account.balance.world.other")
                            : (player.equals(sender) ? "account.balance.self" : "account.balance.other");

                    plugin.bundle().sendMessage(sender, message,
                            Placeholder.parsed("player", String.valueOf(player.getName())),
                            Placeholder.parsed("balance", controller.format(account.getBalance(), locale)),
                            Placeholder.parsed("currency", account.getBalance().intValue() == 1
                                    ? controller.getCurrencyNameSingular(locale)
                                    : controller.getCurrencyNamePlural(locale)),
                            Placeholder.parsed("symbol", controller.getCurrencySymbol()),
                            Placeholder.parsed("world", world != null ? world.getName() : "null"));

                }, () -> plugin.bundle().sendMessage(sender, world != null
                                ? (player.equals(sender) ? "account.not-found.world.self" : "account.not-found.world.other")
                                : (player.equals(sender) ? "account.not-found.self" : "account.not-found.other"),
                        Placeholder.parsed("player", String.valueOf(player.getName())),
                        Placeholder.parsed("world", world != null ? world.getName() : "null"))));

        return Command.SINGLE_SUCCESS;
    }
}
