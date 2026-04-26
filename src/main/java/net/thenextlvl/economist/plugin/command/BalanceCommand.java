package net.thenextlvl.economist.plugin.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import core.paper.brigadier.arguments.OfflinePlayerArgumentType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.currency.Currency;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.argument.CurrencyArgumentType;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class BalanceCommand extends SimpleCommand {
    private BalanceCommand(final EconomistPlugin plugin) {
        super(plugin, "balance", "economist.balance");
    }

    public static LiteralCommandNode<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new BalanceCommand(plugin);
        final var currency = Commands.argument("currency", new CurrencyArgumentType(plugin));
        final var player = Commands.argument("player", OfflinePlayerArgumentType.player())
                .requires(stack -> stack.getSender().hasPermission("economist.balance.others"));
        final var world = Commands.argument("world", ArgumentTypes.world())
                .requires(stack -> stack.getSender().hasPermission("economist.balance.world")
                        && plugin.config.accounts.perWorld);
        return command.create()
                .then(currency.executes(command).then(player.executes(command).then(world.executes(command))))
                .executes(command)
                .build();
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        final var currency = tryGetArgument(context, "currency", Currency.class)
                .orElse(plugin.currencyController().getDefaultCurrency());
        final var player = tryGetArgument(context, "player", OfflinePlayer.class).or(() -> {
            final var executor = sender instanceof final Player p ? p : null;
            return Optional.ofNullable(executor);
        });
        final var world = tryGetArgument(context, "world", World.class).orElse(null);

        return player.map(offline -> balance(context, offline, currency, world)).orElseGet(() -> {
            plugin.bundle().sendMessage(sender, "player.define");
            return 0;
        });
    }

    private int balance(final CommandContext<CommandSourceStack> context, final OfflinePlayer player, final Currency currency, @Nullable final World world) {
        final var sender = context.getSource().getSender();
        final var controller = plugin.economyController();
        Optional.ofNullable(world)
                .map(w -> controller.resolveAccount(player, w))
                .orElseGet(() -> controller.resolveAccount(player))
                .thenAccept(optional -> optional.ifPresentOrElse(account -> {
                    final var message = world != null
                            ? (player.equals(sender) ? "account.balance.world.self" : "account.balance.world.other")
                            : (player.equals(sender) ? "account.balance.self" : "account.balance.other");

                    final var balance = account.getBalance(currency);
                    plugin.bundle().sendMessage(sender, message,
                            Placeholder.parsed("player", String.valueOf(player.getName())),
                            Placeholder.component("balance", currency.format(balance, sender)),
                            Placeholder.component("currency", balance.doubleValue() == 1
                                    ? currency.getDisplayNameSingular(sender).orElse(Component.empty())
                                    : currency.getDisplayNamePlural(sender).orElse(Component.empty())),
                            Placeholder.component("symbol", currency.getSymbol()),
                            Placeholder.parsed("world", world != null ? world.getName() : "null"));

                }, () -> plugin.bundle().sendMessage(sender, world != null
                                ? (player.equals(sender) ? "account.not-found.world.self" : "account.not-found.world.other")
                                : (player.equals(sender) ? "account.not-found.self" : "account.not-found.other"),
                        Placeholder.parsed("player", String.valueOf(player.getName())),
                        Placeholder.parsed("world", world != null ? world.getName() : "null"))));

        return SINGLE_SUCCESS;
    }
}
