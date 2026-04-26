package net.thenextlvl.economist.plugin.command.bank;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.Account;
import net.thenextlvl.economist.TransactionResult;
import net.thenextlvl.economist.bank.Bank;
import net.thenextlvl.economist.currency.Currency;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;
import org.bukkit.entity.Player;

import static net.thenextlvl.economist.plugin.command.bank.BankSupport.NAME_ARGUMENT;

final class BankDepositCommand extends SimpleCommand {
    private BankDepositCommand(final EconomistPlugin plugin) {
        super(plugin, "deposit", "economist.bank.deposit");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new BankDepositCommand(plugin);
        final var amount = Commands.argument("amount", DoubleArgumentType.doubleArg(plugin.config.minimumPayment));
        final var name = Commands.argument(NAME_ARGUMENT, StringArgumentType.word())
                .requires(stack -> stack.getSender().hasPermission("economist.bank.deposit.others"));
        return command.create()
                .then(amount.executes(command)
                        .then(Commands.literal("name").then(name.executes(command))));
    }

    @Override
    protected boolean canUse(final CommandSourceStack source) {
        return super.canUse(source) && source.getSender() instanceof Player;
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = (Player) context.getSource().getSender();
        final var amount = context.getArgument("amount", Double.class);
        final var currency = BankSupport.currency(plugin);
        plugin.economyController().resolveAccount(sender).thenAccept(optional -> optional.ifPresentOrElse(account ->
                        BankSupport.resolveBankTarget(plugin, context).thenAccept(optionalBank ->
                                optionalBank.ifPresentOrElse(bank -> deposit(sender, context, account, bank, amount, currency),
                                        () -> BankSupport.sendBankNotFound(plugin, sender, context))),
                () -> plugin.bundle().sendMessage(sender, "account.not-found.self")));
        return SINGLE_SUCCESS;
    }

    private void deposit(final Player sender, final CommandContext<CommandSourceStack> context,
                         final Account account, final Bank bank, final double amount, final Currency currency) {
        final var admin = BankSupport.findArgument(context, NAME_ARGUMENT, String.class).isPresent()
                && sender.hasPermission("economist.bank.deposit.others");
        if (!admin && !bank.canDeposit(sender, amount, currency)) {
            plugin.bundle().sendMessage(sender, "bank.access.denied");
            return;
        }
        final var withdrawn = account.withdraw(amount, currency);
        if (!withdrawn.successful()) {
            if (withdrawn.status() == TransactionResult.Status.INSUFFICIENT_FUNDS) {
                plugin.bundle().sendMessage(sender, "account.funds");
            } else {
                plugin.bundle().sendMessage(sender, "operation.failed");
            }
            return;
        }
        final var deposited = bank.deposit(amount, currency);
        if (!deposited.successful()) {
            account.deposit(amount, currency);
            plugin.bundle().sendMessage(sender, "operation.failed");
            return;
        }
        plugin.bundle().sendMessage(sender, "bank.deposit",
                Placeholder.parsed("bank", bank.getName()),
                Placeholder.component("amount", currency.format(amount, sender)),
                Placeholder.component("balance", currency.format(deposited.balance(), sender)),
                Placeholder.component("symbol", currency.getSymbol()));
    }
}
