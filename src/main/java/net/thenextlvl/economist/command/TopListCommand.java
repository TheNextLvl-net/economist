package net.thenextlvl.economist.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.Account;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class TopListCommand {
    private final EconomistPlugin plugin;

    public void register() {
        var command = Commands.literal("balance-top")
                .requires(stack -> stack.getSender().hasPermission("economist.balance-top"))
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                        .then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> stack.getSender().hasPermission("economist.balance-top.world"))
                                .executes(context -> {
                                    var world = context.getArgument("world", World.class);
                                    var page = context.getArgument("page", int.class);
                                    return top(context, world, page);
                                }))
                        .executes(context -> {
                            var page = context.getArgument("page", int.class);
                            return top(context, null, page);
                        }))
                .executes(context -> top(context, null, 1))
                .build();
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event ->
                event.registrar().register(command, "Shows the balance top-list", List.of("baltop"))));
    }

    private int top(CommandContext<CommandSourceStack> context, @Nullable World world, int page) {
        var sender = context.getSource().getSender();
        int pageEntryCount = plugin.config().balanceTop().entriesPerPage();
        var index = pageEntryCount * (page - 1);
        getOrdered(world, index, pageEntryCount)
                .thenAccept(accounts -> top(sender, accounts, world))
                .exceptionally(throwable -> {
                    plugin.getComponentLogger().error("Failed to retrieve top-list", throwable);
                    return null;
                });
        return 0;
    }

    private void top(CommandSender sender, List<Account> accounts, @Nullable World world) {
        if (accounts.isEmpty()) {
            plugin.bundle().sendMessage(sender, "balance.top-list.empty");
            return;
        }

        plugin.bundle().sendMessage(sender, world != null ? "balance.top-list.header.world" : "balance.top-list.header",
                Placeholder.parsed("world", world != null ? world.key().asString() : "null"));

        var locale = sender instanceof Player player ? player.locale() : Locale.US;
        for (int i = 0; i < accounts.size(); i++) {
            var account = accounts.get(i);
            var player = plugin.getServer().getOfflinePlayer(account.getOwner());
            plugin.bundle().sendMessage(sender, "balance.top-list",
                    Placeholder.parsed("balance", plugin.economyController().format(account.getBalance(), locale)),
                    Placeholder.parsed("player", player.getName() != null ? player.getName() : "null"),
                    Placeholder.parsed("rank", String.valueOf(i + 1)),
                    Placeholder.parsed("symbol", plugin.economyController().getCurrencySymbol()));
        }
    }

    private CompletableFuture<@Unmodifiable List<Account>> getOrdered(@Nullable World world, int start, int limit) {
        if (world == null) return plugin.economyController().tryGetOrdered(start, limit);
        return plugin.economyController().tryGetOrdered(world, start, limit);
    }
}
