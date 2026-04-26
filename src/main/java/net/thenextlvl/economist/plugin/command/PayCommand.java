package net.thenextlvl.economist.plugin.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import core.paper.brigadier.arguments.OfflinePlayerArgumentType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.Account;
import net.thenextlvl.economist.TransactionResult;
import net.thenextlvl.economist.currency.Currency;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.argument.CurrencyArgumentType;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class PayCommand extends SimpleCommand {
    private PayCommand(final EconomistPlugin plugin) {
        super(plugin, "pay", "economist.pay");
    }

    public static LiteralCommandNode<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new PayCommand(plugin);
        final var amountArgument = DoubleArgumentType.doubleArg(plugin.config.minimumPayment);
        return command.create()
                .then(Commands.argument("player", OfflinePlayerArgumentType.player())
                        .then(Commands.argument("currency", new CurrencyArgumentType(plugin))
                                .then(Commands.argument("amount", amountArgument)
                                        .executes(command)
                                        .then(Commands.argument("world", ArgumentTypes.world())
                                                .requires(stack -> stack.getSender().hasPermission("economist.pay.world")
                                                        && plugin.config.accounts.perWorld)
                                                .executes(command))))
                        .then(Commands.argument("amount", amountArgument)
                                .executes(command)
                                .then(Commands.argument("world", ArgumentTypes.world())
                                        .requires(stack -> stack.getSender().hasPermission("economist.pay.world")
                                                && plugin.config.accounts.perWorld)
                                        .executes(command))))
                .then(Commands.argument("players", ArgumentTypes.players())
                        .then(Commands.argument("currency", new CurrencyArgumentType(plugin))
                                .then(Commands.argument("amount", amountArgument)
                                        .executes(command)
                                        .then(Commands.argument("world", ArgumentTypes.world())
                                                .requires(stack -> stack.getSender().hasPermission("economist.pay.world")
                                                        && plugin.config.accounts.perWorld)
                                                .executes(command))))
                        .then(Commands.argument("amount", amountArgument)
                                .executes(command)
                                .then(Commands.argument("world", ArgumentTypes.world())
                                        .requires(stack -> stack.getSender().hasPermission("economist.pay.world")
                                                && plugin.config.accounts.perWorld)
                                        .executes(command))))
                .build();
    }

    @Override
    protected boolean canUse(final CommandSourceStack source) {
        return super.canUse(source) && source.getSender() instanceof Player;
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final List<OfflinePlayer> players = resolveArgument(context, "players", PlayerSelectorArgumentResolver.class)
                .map(resolved -> new ArrayList<OfflinePlayer>(resolved))
                .orElseGet(() -> tryGetArgument(context, "player", OfflinePlayer.class)
                        .map(player -> new ArrayList<>(List.of(player)))
                        .orElseGet(ArrayList::new));
        final var currency = tryGetArgument(context, "currency", Currency.class)
                .orElse(plugin.currencyController().getDefaultCurrency());
        final var world = tryGetArgument(context, "world", World.class).orElse(null);
        return pay(context, players, currency, world);
    }

    private int pay(final CommandContext<CommandSourceStack> context, final List<? extends OfflinePlayer> players,
                    final Currency currency, @Nullable final World world) {
        final var sender = (Player) context.getSource().getSender();

        final var amount = context.getArgument("amount", Double.class);
        final var minimum = sender.hasPermission("economist.loan") ? -plugin.config.maxLoanAmount : 0;

        if (players.isEmpty()) {
            plugin.bundle().sendMessage(sender, "player.define");
            return 0;
        } else if (players.size() == 1 && players.getFirst().equals(sender)) {
            plugin.bundle().sendMessage(sender, "player.pay.self");
            return 0;
        } else if (players.size() > 1) players.remove(sender);

        resolveAccount(sender, world).thenAccept(optional -> optional.ifPresentOrElse(account ->
                        players.forEach(player -> resolveAccount(player, world).thenAccept(optional1 ->
                                optional1.ifPresentOrElse(target ->
                                                pay(sender, player, account, target, amount, currency, minimum),
                                        () -> missingAccount(world, sender, player, plugin)
                                ))),
                () -> missingAccount(world, sender, sender, plugin)));

        return players.size();
    }

    private CompletableFuture<Optional<Account>> resolveAccount(final OfflinePlayer player, @Nullable final World world) {
        if (world == null) return plugin.economyController().resolveAccount(player);
        return plugin.economyController().resolveAccount(player, world);
    }

    private void pay(final Player sender, final OfflinePlayer player, final Account source, final Account target,
                     final double amount, final Currency currency, final double minimum) {
        if (!source.canHold(currency) || !target.canHold(currency)) {
            return;
        }

        if (source.getBalance(currency).doubleValue() - amount < minimum) {
            plugin.bundle().sendMessage(sender, "account.funds");
            return;
        }

        final var withdrawn = source.withdraw(amount, currency);
        if (!withdrawn.successful()) {
            if (withdrawn.status() == TransactionResult.Status.INSUFFICIENT_FUNDS) {
                plugin.bundle().sendMessage(sender, "account.funds");
            } else if (withdrawn.status() == TransactionResult.Status.OUT_OF_BOUNDS) {
                plugin.bundle().sendMessage(sender, "account.balance-range.invalid");
            }
            return;
        }
        final var deposited = target.deposit(amount, currency);
        if (!deposited.successful()) {
            source.deposit(amount, currency);
            if (deposited.status() == TransactionResult.Status.OUT_OF_BOUNDS) {
                plugin.bundle().sendMessage(sender, "account.balance-range.invalid");
            } else {
                plugin.bundle().sendMessage(sender, "operation.failed");
            }
            return;
        }

        plugin.bundle().sendMessage(sender, "player.pay.outgoing",
                Placeholder.component("amount", currency.format(amount, sender)),
                Placeholder.parsed("player", player.getName() != null ? player.getName() : player.getUniqueId().toString()),
                Placeholder.component("symbol", currency.getSymbol()));

        final var online = player.getPlayer();
        if (online != null) plugin.bundle().sendMessage(online, "player.pay.incoming",
                Placeholder.component("amount", currency.format(amount, online)),
                Placeholder.component("symbol", currency.getSymbol()),
                Placeholder.parsed("player", sender.getName()));
    }

    private static void missingAccount(@Nullable final World world, final Player sender, final OfflinePlayer player, final EconomistPlugin plugin) {
        plugin.bundle().sendMessage(sender, world != null
                        ? (player.equals(sender) ? "account.not-found.world.self" : "account.not-found.world.other")
                        : (player.equals(sender) ? "account.not-found.self" : "account.not-found.other"),
                Placeholder.parsed("player", player.getName() != null ? player.getName() : player.getUniqueId().toString()),
                Placeholder.parsed("world", world != null ? world.getName() : "null"));
    }
}
