package net.thenextlvl.economist.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.EconomistPlugin;
import net.thenextlvl.economist.api.Account;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class BalanceTopCommand {
    public static LiteralCommandNode<CommandSourceStack> create(EconomistPlugin plugin) {
        return Commands.literal("balance-top")
                .requires(stack -> stack.getSender().hasPermission("economist.balance-top"))
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                        .then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> stack.getSender().hasPermission("economist.balance-top.world"))
                                .executes(context -> {
                                    var world = context.getArgument("world", World.class);
                                    var page = context.getArgument("page", int.class);
                                    return top(context, world, page, plugin);
                                }))
                        .executes(context -> {
                            var page = context.getArgument("page", int.class);
                            return top(context, null, page, plugin);
                        }))
                .executes(context -> top(context, null, 1, plugin))
                .build();
    }

    private static int top(CommandContext<CommandSourceStack> context, @Nullable World world, int page, EconomistPlugin plugin) {
        var sender = context.getSource().getSender();
        int pageEntryCount = plugin.config.balanceTop.entriesPerPage;
        var index = pageEntryCount * (page - 1);
        getOrdered(world, index, pageEntryCount, plugin)
                .thenAccept(accounts -> top(sender, accounts, index, world, plugin))
                .exceptionally(throwable -> {
                    plugin.getComponentLogger().error("Failed to retrieve top-list", throwable);
                    return null;
                });
        return 0;
    }

    private static void top(CommandSender sender, List<Account> accounts, int index, @Nullable World world, EconomistPlugin plugin) {
        if (accounts.isEmpty()) {
            plugin.bundle().sendMessage(sender, "balance.top-list.empty");
            return;
        }

        var locale = sender instanceof Player player ? player.locale() : Locale.US;
        var decimal = BigDecimal.ZERO;
        try {
            decimal = plugin.dataController().getTotalBalance(world);
        } catch (SQLException e) {
            plugin.getComponentLogger().error("Failed to calculate total balance", e);
        }
        var totalBalance = decimal.doubleValue();

        plugin.bundle().sendMessage(sender, world != null ? "balance.top-list.header.world" : "balance.top-list.header",
                Placeholder.parsed("world", world != null ? world.getName() : "null"));
        plugin.bundle().sendMessage(sender, world != null ? "balance.top-list.total.world" : "balance.top-list.total",
                Placeholder.parsed("symbol", plugin.economyController().getCurrencySymbol()),
                Placeholder.parsed("total", plugin.economyController().format(totalBalance, locale)),
                Placeholder.parsed("world", world != null ? world.getName() : "null"));

        for (int i = 0; i < accounts.size(); i++) {
            var account = accounts.get(i);
            var player = plugin.getServer().getOfflinePlayer(account.getOwner());
            var worth = totalBalance == 0 ? 0d : (account.getBalance().doubleValue() / totalBalance) * 100d;
            plugin.bundle().sendMessage(sender, "balance.top-list",
                    Placeholder.parsed("balance", plugin.economyController().format(account.getBalance(), locale)),
                    Placeholder.parsed("player", player.getName() != null ? player.getName() : player.getUniqueId().toString()),
                    Placeholder.parsed("rank", String.valueOf(index + (i + 1))),
                    Placeholder.parsed("symbol", plugin.economyController().getCurrencySymbol()),
                    Placeholder.parsed("worth", String.format(locale, "%.2f%%", worth)));
        }
    }

    private static CompletableFuture<@Unmodifiable List<Account>> getOrdered(@Nullable World world, int start, int limit, EconomistPlugin plugin) {
        if (world == null) return plugin.economyController().tryGetOrdered(start, limit);
        return plugin.economyController().tryGetOrdered(world, start, limit);
    }
}
