package net.thenextlvl.economist.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class BalanceCommand {
    private final EconomistPlugin plugin;

    public void register() {
        var command = Commands.literal("balance")
                .requires(stack -> stack.getSender().hasPermission("economist.admin"))
                .then(Commands.argument("player", CustomArgumentTypes.cachedOfflinePlayer())
                        .then(Commands.argument("world", ArgumentTypes.world())
                                .executes(context -> {
                                    var player = context.getArgument("player", OfflinePlayer.class);
                                    var world = context.getArgument("world", World.class);
                                    return balance(context, player, world);
                                }))
                        .executes(context -> {
                            var target = context.getArgument("player", OfflinePlayer.class);
                            if (context.getSource().getSender() instanceof Player player)
                                return balance(context, target, player.getWorld());
                            return balance(context, target, null);
                        }))
                .executes(context -> {
                    if (context.getSource().getSender() instanceof Player player)
                        return balance(context, player, player.getWorld());
                    throw new IllegalArgumentException("No player defined");
                })
                .build();
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event ->
                event.registrar().register(command, "Display a players balance", List.of("money"))));
    }

    private int balance(CommandContext<CommandSourceStack> context, OfflinePlayer player, @Nullable World world) {
        var sender = context.getSource().getSender();
        var controller = plugin.economyController();
        Optional.ofNullable(world)
                .map(w -> controller.tryGetAccount(player, w))
                .orElseGet(() -> controller.tryGetAccount(player))
                .thenAccept(optional -> optional.ifPresentOrElse(account -> {
                    var locale = sender instanceof Player p ? p.locale() : Locale.US;
                    var message = world != null && world.equals(sender instanceof Player p ? p.getWorld() : null)
                            ? (player.equals(sender) ? "account.balance.self" : "account.balance.other")
                            : (player.equals(sender) ? "account.balance.world.self" : "account.balance.world.other");
                    plugin.bundle().sendMessage(sender, message,
                            Placeholder.parsed("player", String.valueOf(player.getName())),
                            Placeholder.parsed("balance", controller.format(account.getBalance(), locale)),
                            Placeholder.parsed("currency", account.getBalance().intValue() == 1
                                    ? controller.getCurrencyNameSingular(locale)
                                    : controller.getCurrencyNamePlural(locale)),
                            Placeholder.parsed("symbol", controller.getCurrencySymbol()));
                }, () -> plugin.bundle().sendMessage(sender,
                        world != null ? "account.not-found.world" : "account.not-found",
                        Placeholder.parsed("player", String.valueOf(player.getName())),
                        Placeholder.parsed("world", world != null ? world.getName() : "null"))));
        return Command.SINGLE_SUCCESS;
    }
}
