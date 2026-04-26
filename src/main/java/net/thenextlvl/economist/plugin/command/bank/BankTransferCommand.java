package net.thenextlvl.economist.plugin.command.bank;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.TransactionResult;
import net.thenextlvl.economist.bank.Bank;
import net.thenextlvl.economist.currency.Currency;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static net.thenextlvl.economist.plugin.command.bank.BankSupport.BANK_ARGUMENT;
import static net.thenextlvl.economist.plugin.command.bank.BankSupport.TARGET_ARGUMENT;

final class BankTransferCommand extends SimpleCommand {
    private BankTransferCommand(final EconomistPlugin plugin) {
        super(plugin, "transfer", "economist.bank.transfer");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new BankTransferCommand(plugin);
        final var target = Commands.argument(TARGET_ARGUMENT, com.mojang.brigadier.arguments.StringArgumentType.word());
        final var amount = Commands.argument("amount", DoubleArgumentType.doubleArg(plugin.config.minimumPayment));
        final var sourceBank = Commands.argument(BANK_ARGUMENT, com.mojang.brigadier.arguments.StringArgumentType.word())
                .requires(stack -> stack.getSender().hasPermission("economist.bank.transfer.others"));
        return command.create()
                .then(target.then(amount.executes(command)))
                .then(Commands.literal("name")
                        .then(sourceBank.then(target.then(amount.executes(command)))));
    }

    @Override
    protected boolean canUse(final CommandSourceStack source) {
        return super.canUse(source) && source.getSender() instanceof Player;
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = (Player) context.getSource().getSender();
        final var targetName = context.getArgument(TARGET_ARGUMENT, String.class);
        final var amount = context.getArgument("amount", Double.class);
        final var currency = BankSupport.currency(plugin);
        resolveTransferSource(context, sender).thenAccept(optionalSource -> optionalSource.ifPresentOrElse(source ->
                        plugin.bankController().resolveBank(targetName).thenAccept(optionalTarget ->
                                optionalTarget.ifPresentOrElse(target -> transfer(sender, context, source, target, amount, currency),
                                        () -> plugin.bundle().sendMessage(sender, "bank.not-found.name",
                                                Placeholder.parsed("bank", targetName)))),
                () -> BankSupport.sendBankNotFound(plugin, sender, context)));
        return SINGLE_SUCCESS;
    }

    private CompletableFuture<Optional<Bank>> resolveTransferSource(final CommandContext<CommandSourceStack> context,
                                                                    final Player sender) {
        return BankSupport.findArgument(context, BANK_ARGUMENT, String.class)
                .map(plugin.bankController()::resolveBank)
                .orElseGet(() -> plugin.bankController().resolveBank(sender));
    }

    private void transfer(final Player sender, final CommandContext<CommandSourceStack> context,
                          final Bank source, final Bank target, final double amount, final Currency currency) {
        if (source.getName().equalsIgnoreCase(target.getName())) {
            plugin.bundle().sendMessage(sender, "operation.failed");
            return;
        }
        final var admin = BankSupport.findArgument(context, BANK_ARGUMENT, String.class).isPresent()
                && sender.hasPermission("economist.bank.transfer.others");
        if (!admin && !source.canWithdraw(sender, amount, currency)) {
            plugin.bundle().sendMessage(sender, "bank.access.denied");
            return;
        }
        final var withdrawn = source.withdraw(amount, currency);
        if (!withdrawn.successful()) {
            if (withdrawn.status() == TransactionResult.Status.INSUFFICIENT_FUNDS) {
                plugin.bundle().sendMessage(sender, "account.funds");
            } else {
                plugin.bundle().sendMessage(sender, "operation.failed");
            }
            return;
        }
        final var deposited = target.deposit(amount, currency);
        if (!deposited.successful()) {
            source.deposit(amount, currency);
            plugin.bundle().sendMessage(sender, "operation.failed");
            return;
        }
        plugin.bundle().sendMessage(sender, "bank.transfer",
                Placeholder.parsed("bank", source.getName()),
                Placeholder.parsed("target", target.getName()),
                Placeholder.component("amount", currency.format(amount, sender)),
                Placeholder.component("symbol", currency.getSymbol()));
    }
}
