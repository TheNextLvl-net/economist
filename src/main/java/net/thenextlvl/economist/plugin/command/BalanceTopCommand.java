package net.thenextlvl.economist.plugin.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.Account;
import net.thenextlvl.economist.currency.Currency;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.argument.CurrencyArgumentType;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class BalanceTopCommand extends SimpleCommand {
    private BalanceTopCommand(final EconomistPlugin plugin) {
        super(plugin, "balance-top", "economist.balance-top");
    }

    public static LiteralCommandNode<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new BalanceTopCommand(plugin);
        final var currency = Commands.argument("currency", new CurrencyArgumentType(plugin));
        return command.create()
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                        .executes(command)
                        .then(Commands.argument("world", ArgumentTypes.world())
                                .requires(stack -> stack.getSender().hasPermission("economist.balance-top.world")
                                        && plugin.config.accounts.perWorld)
                                .executes(command)))
                .then(currency.executes(command)
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(command)
                                .then(Commands.argument("world", ArgumentTypes.world())
                                        .requires(stack -> stack.getSender().hasPermission("economist.balance-top.world")
                                                && plugin.config.accounts.perWorld)
                                        .executes(command))))
                .executes(command)
                .build();
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var currency = tryGetArgument(context, "currency", Currency.class)
                .orElse(plugin.currencyController().getDefaultCurrency());
        final var page = tryGetArgument(context, "page", Integer.class).orElse(1);
        final var world = tryGetArgument(context, "world", World.class).orElse(null);
        return top(context, currency, world, page);
    }

    private int top(final CommandContext<CommandSourceStack> context, final Currency currency,
                    @Nullable final World world, final int page) {
        final var sender = context.getSource().getSender();
        final int pageEntryCount = plugin.config.pagination.entriesPerPage;
        final var index = pageEntryCount * (page - 1);
        getOrdered(currency, world, index, pageEntryCount)
                .thenAccept(accounts -> top(sender, accounts, currency, index, world))
                .exceptionally(throwable -> {
                    plugin.getComponentLogger().error("Failed to retrieve top-list", throwable);
                    return null;
                });
        return 0;
    }

    private void top(final CommandSender sender, final List<Account> accounts, final Currency currency,
                     final int index, @Nullable final World world) {
        if (accounts.isEmpty()) {
            plugin.bundle().sendMessage(sender, "balance.top-list.empty");
            return;
        }

        var decimal = BigDecimal.ZERO;
        try {
            decimal = plugin.dataController().getTotalBalance(currency, world);
        } catch (final SQLException e) {
            plugin.getComponentLogger().error("Failed to calculate total balance", e);
        }
        final var totalBalance = decimal.doubleValue();

        plugin.bundle().sendMessage(sender, world != null ? "balance.top-list.header.world" : "balance.top-list.header",
                Placeholder.parsed("world", world != null ? world.getName() : "null"));
        plugin.bundle().sendMessage(sender, world != null ? "balance.top-list.total.world" : "balance.top-list.total",
                Placeholder.component("symbol", currency.getSymbol()),
                Placeholder.component("total", currency.format(totalBalance, sender)),
                Placeholder.parsed("world", world != null ? world.getName() : "null"));

        for (int i = 0; i < accounts.size(); i++) {
            final var account = accounts.get(i);
            final var player = plugin.getServer().getOfflinePlayer(account.getOwner());
            final var balance = account.getBalance(currency);
            final var worth = totalBalance == 0 ? 0d : (balance.doubleValue() / totalBalance) * 100d;
            plugin.bundle().sendMessage(sender, "balance.top-list",
                    Placeholder.component("balance", currency.format(balance, sender)),
                    Placeholder.parsed("player", player.getName() != null ? player.getName() : player.getUniqueId().toString()),
                    Placeholder.parsed("rank", String.valueOf(index + (i + 1))),
                    Placeholder.component("symbol", currency.getSymbol()),
                    Placeholder.parsed("worth", String.format(sender.getOrDefault(Identity.LOCALE, Locale.US), "%.2f%%", worth)));
        }
    }

    private CompletableFuture<@Unmodifiable List<Account>> getOrdered(final Currency currency, @Nullable final World world,
                                                                      final int start, final int limit) {
        if (world == null) return plugin.economyController().resolveOrdered(currency, start, limit);
        return plugin.economyController().resolveOrdered(currency, world, start, limit);
    }
}
