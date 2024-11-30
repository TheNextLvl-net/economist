package net.thenextlvl.economist.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import core.paper.command.CustomArgumentTypes;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class BalanceCommand {
    private final EconomistPlugin plugin;

    public void register() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event ->
                event.registrar().register(create(), "Display a players balance", plugin.config().balanceAliases())));
    }

    LiteralCommandNode<CommandSourceStack> create() {
        return Commands.literal("balance")
                .requires(stack -> stack.getSender().hasPermission("economist.balance"))
                .then(Commands.argument("player", CustomArgumentTypes.cachedOfflinePlayer())
                        .requires(stack -> stack.getSender().hasPermission("economist.balance.others"))
                        .then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> stack.getSender().hasPermission("economist.balance.world"))
                                .executes(context -> {
                                    var player = context.getArgument("player", OfflinePlayer.class);
                                    var world = context.getArgument("world", World.class);
                                    return balance(context, player, world);
                                }))
                        .executes(context -> {
                            var player = context.getArgument("player", OfflinePlayer.class);
                            return balance(context, player, null);
                        }))
                .executes(context -> {
                    var sender = context.getSource().getSender();
                    if (sender instanceof Player player) return balance(context, player, null);
                    plugin.bundle().sendMessage(sender, "player.define");
                    return 0;
                })
                .build();
    }

    private int balance(CommandContext<CommandSourceStack> context, OfflinePlayer player, @Nullable World world) {
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
