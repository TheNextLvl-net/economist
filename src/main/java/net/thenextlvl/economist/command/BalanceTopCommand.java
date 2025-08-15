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
import net.thenextlvl.economist.api.currency.Currency;
import net.thenextlvl.economist.command.argument.CurrencyArgument;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;

@NullMarked
public class BalanceTopCommand {
    public static LiteralCommandNode<CommandSourceStack> create(EconomistPlugin plugin) {
        return Commands.literal("balance-top")
                .requires(stack -> stack.getSender().hasPermission("economist.balance-top"))
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                        .then(Commands.argument("currency", new CurrencyArgument(plugin))
                                .then(Commands.argument("world", ArgumentTypes.world())
                                        .requires(stack -> stack.getSender().hasPermission("economist.balance-top.world") && plugin.config.accounts.perWorld)
                                        .executes(context -> {
                                            var world = context.getArgument("world", World.class);
                                            var currency = context.getArgument("currency", Currency.class);
                                            var page = context.getArgument("page", int.class);
                                            return top(context, currency, world, page, plugin);
                                        }))
                                .executes(context -> {
                                    var currency = context.getArgument("currency", Currency.class);
                                    var page = context.getArgument("page", int.class);
                                    return top(context, currency, null, page, plugin);
                                }))
                        .executes(context -> {
                            var page = context.getArgument("page", int.class);
                            return top(context, plugin.currencyHolder().getDefaultCurrency(), null, page, plugin);
                        }))
                .executes(context -> top(context, plugin.currencyHolder().getDefaultCurrency(), null, 1, plugin))
                .build();
    }

    private static int top(CommandContext<CommandSourceStack> context, Currency currency, @Nullable World world, int page, EconomistPlugin plugin) {
        var sender = context.getSource().getSender();
        int pageEntryCount = plugin.config.balanceTop.entriesPerPage;
        var index = pageEntryCount * (page - 1);
        plugin.economyController().tryGetOrdered(currency, world, index, pageEntryCount)
                .thenAccept(accounts -> top(sender, accounts, index, currency, world, plugin))
                .exceptionally(throwable -> {
                    plugin.getComponentLogger().error("Failed to retrieve top-list", throwable);
                    return null;
                });
        return 0;
    }

    private static void top(CommandSender sender, List<Account> accounts, int index, Currency currency, @Nullable World world, EconomistPlugin plugin) {
        if (accounts.isEmpty()) {
            plugin.bundle().sendMessage(sender, "balance.top-list.empty");
            return;
        }

        var locale = sender instanceof Player player ? player.locale() : Locale.US;
        var balance = plugin.dataController().getTotalBalance(currency, world).doubleValue();

        plugin.bundle().sendMessage(sender, world != null ? "balance.top-list.header.world" : "balance.top-list.header",
                Placeholder.parsed("world", world != null ? world.getName() : "null"));
        plugin.bundle().sendMessage(sender, world != null ? "balance.top-list.total.world" : "balance.top-list.total",
                Placeholder.component("symbol", currency.getSymbol()),
                Placeholder.component("total", currency.format(balance, locale)),
                Placeholder.parsed("world", world != null ? world.getName() : "null"));

        for (int i = 0; i < accounts.size(); i++) {
            var account = accounts.get(i);
            var player = plugin.getServer().getOfflinePlayer(account.getOwner());
            var worth = balance == 0 ? 0d : (account.getBalance(currency).doubleValue() / balance) * 100d;
            plugin.bundle().sendMessage(sender, "balance.top-list",
                    Placeholder.component("balance", currency.format(account.getBalance(currency), locale)),
                    Placeholder.parsed("player", player.getName() != null ? player.getName() : player.getUniqueId().toString()),
                    Placeholder.parsed("rank", String.valueOf(index + (i + 1))),
                    Placeholder.component("symbol", currency.getSymbol()),
                    Placeholder.parsed("worth", String.format(locale, "%.2f%%", worth)));
        }
    }
}
