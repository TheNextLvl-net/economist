package net.thenextlvl.economist.plugin.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
import net.thenextlvl.economist.plugin.command.brigadier.BrigadierCommand;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public final class EconomyCommand extends BrigadierCommand {
    private EconomyCommand(final EconomistPlugin plugin) {
        super(plugin, "economy", "economist.admin");
    }

    public static LiteralCommandNode<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new EconomyCommand(plugin);
        return command.create()
                .then(new GiveCommand(plugin).createCommand())
                .then(new ResetCommand(plugin).createCommand())
                .then(new SetCommand(plugin).createCommand())
                .then(new TakeCommand(plugin).createCommand())
                .build();
    }

    private static int execute(final EconomistPlugin plugin, final CommandContext<CommandSourceStack> context,
                               final String successMessage, final Collection<? extends OfflinePlayer> players,
                               final Number amount, final Currency currency, @Nullable final World world,
                               final AccountAction action) {
        final var sender = context.getSource().getSender();
        if (!players.isEmpty()) players.forEach(player -> (world != null
                ? plugin.economyController().resolveAccount(player, world)
                : plugin.economyController().resolveAccount(player))
                .thenAccept(optional -> optional.ifPresentOrElse(account -> {
                    final var result = account.canHold(currency)
                            ? action.execute(account, amount, currency)
                            : TransactionResult.unsupported(currency);
                    if (!result.successful()) {
                        if (result.status() == TransactionResult.Status.INSUFFICIENT_FUNDS) {
                            plugin.bundle().sendMessage(sender, "account.funds");
                        }
                        return;
                    }
                    plugin.bundle().sendMessage(sender, successMessage,
                            Placeholder.parsed("world", world != null ? world.getName() : "null"),
                            Placeholder.parsed("player", String.valueOf(player.getName())),
                            Placeholder.component("balance", currency.format(result.balance(), sender)),
                            Placeholder.component("amount", currency.format(result.amount(), sender)),
                            Placeholder.component("symbol", currency.getSymbol()));
                }, () -> plugin.bundle().sendMessage(sender, world != null
                                ? (sender.equals(player) ? "account.not-found.world.self" : "account.not-found.world.other")
                                : (sender.equals(player) ? "account.not-found.self" : "account.not-found.other"),
                        Placeholder.parsed("player", String.valueOf(player.getName())),
                        Placeholder.parsed("world", world != null ? world.getName() : "null")))));
        else plugin.bundle().sendMessage(sender, "player.define");
        return SimpleCommand.SINGLE_SUCCESS;
    }

    private abstract static class AmountCommand extends SimpleCommand {
        private final String successMessage;
        private final String successMessageWorld;
        private final double minimum;
        private final AccountAction action;

        private AmountCommand(final EconomistPlugin plugin, final String name, final String successMessage,
                              final String successMessageWorld, final double minimum,
                              final AccountAction action) {
            super(plugin, name, null);
            this.successMessage = successMessage;
            this.successMessageWorld = successMessageWorld;
            this.minimum = minimum;
            this.action = action;
        }

        protected LiteralArgumentBuilder<CommandSourceStack> createCommand() {
            final var currency = Commands.argument("currency", new CurrencyArgumentType(plugin)).executes(this);
            return create()
                    .then(Commands.argument("player", OfflinePlayerArgumentType.player())
                            .then(currency.then(Commands.argument("amount", DoubleArgumentType.doubleArg(minimum))
                                    .executes(this)
                                    .then(Commands.argument("world", ArgumentTypes.world())
                                            .requires(stack -> plugin.config.accounts.perWorld)
                                            .executes(this))))
                            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(minimum))
                                    .executes(this)
                                    .then(Commands.argument("world", ArgumentTypes.world())
                                            .requires(stack -> plugin.config.accounts.perWorld)
                                            .executes(this))))
                    .then(Commands.argument("players", ArgumentTypes.players())
                            .then(Commands.argument("currency", new CurrencyArgumentType(plugin))
                                    .executes(this)
                                    .then(Commands.argument("amount", DoubleArgumentType.doubleArg(minimum))
                                            .executes(this)
                                            .then(Commands.argument("world", ArgumentTypes.world())
                                                    .requires(stack -> plugin.config.accounts.perWorld)
                                                    .executes(this))))
                            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(minimum))
                                    .executes(this)
                                    .then(Commands.argument("world", ArgumentTypes.world())
                                            .requires(stack -> plugin.config.accounts.perWorld)
                                            .executes(this))));
        }

        @Override
        public int run(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            final var players = resolveArgument(context, "players", PlayerSelectorArgumentResolver.class)
                    .map(List::<OfflinePlayer>copyOf)
                    .orElseGet(() -> tryGetArgument(context, "player", OfflinePlayer.class)
                            .map(List::of)
                            .orElseGet(List::of));
            final var amount = context.getArgument("amount", Double.class);
            final var currency = tryGetArgument(context, "currency", Currency.class)
                    .orElse(plugin.currencyController().getDefaultCurrency());
            final var world = tryGetArgument(context, "world", World.class).orElse(null);
            final var successMessage = world != null ? successMessageWorld : this.successMessage;
            return execute(plugin, context, successMessage, players, amount, currency, world, action);
        }
    }

    private static final class GiveCommand extends AmountCommand {
        private GiveCommand(final EconomistPlugin plugin) {
            super(plugin, "give", "balance.deposited", "balance.deposited.world",
                    plugin.config.minimumPayment, Account::deposit);
        }
    }

    private static final class TakeCommand extends AmountCommand {
        private TakeCommand(final EconomistPlugin plugin) {
            super(plugin, "take", "balance.withdrawn", "balance.withdrawn.world",
                    plugin.config.minimumPayment, Account::withdraw);
        }
    }

    private static final class SetCommand extends AmountCommand {
        private SetCommand(final EconomistPlugin plugin) {
            super(plugin, "set", "balance.set", "balance.set.world",
                    -Double.MAX_VALUE, Account::setBalance);
        }
    }

    private static final class ResetCommand extends SimpleCommand {
        private ResetCommand(final EconomistPlugin plugin) {
            super(plugin, "reset", null);
        }

        private LiteralArgumentBuilder<CommandSourceStack> createCommand() {
            final var currency = Commands.argument("currency", new CurrencyArgumentType(plugin)).executes(this);
            return create()
                    .then(Commands.argument("player", OfflinePlayerArgumentType.player())
                            .then(currency.then(Commands.argument("world", ArgumentTypes.world())
                                    .requires(stack -> plugin.config.accounts.perWorld)
                                    .executes(this)))
                            .executes(this)
                            .then(Commands.argument("world", ArgumentTypes.world())
                                    .requires(stack -> plugin.config.accounts.perWorld)
                                    .executes(this)))
                    .then(Commands.argument("players", ArgumentTypes.players())
                            .then(Commands.argument("currency", new CurrencyArgumentType(plugin))
                                    .executes(this)
                                    .then(Commands.argument("world", ArgumentTypes.world())
                                            .requires(stack -> plugin.config.accounts.perWorld)
                                            .executes(this)))
                            .executes(this)
                            .then(Commands.argument("world", ArgumentTypes.world())
                                    .requires(stack -> plugin.config.accounts.perWorld)
                                    .executes(this)));
        }

        @Override
        public int run(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            final var players = resolveArgument(context, "players", PlayerSelectorArgumentResolver.class)
                    .map(List::<OfflinePlayer>copyOf)
                    .orElseGet(() -> tryGetArgument(context, "player", OfflinePlayer.class)
                            .map(List::of)
                            .orElseGet(List::of));
            final var currency = tryGetArgument(context, "currency", Currency.class)
                    .orElse(plugin.currencyController().getDefaultCurrency());
            final var world = tryGetArgument(context, "world", World.class).orElse(null);
            final var successMessage = world != null ? "balance.reset.world" : "balance.reset";
            return execute(plugin, context, successMessage, players, plugin.config.startBalance,
                    currency, world, Account::setBalance);
        }
    }

    @FunctionalInterface
    private interface AccountAction {
        TransactionResult execute(Account account, Number amount, Currency currency);
    }
}
