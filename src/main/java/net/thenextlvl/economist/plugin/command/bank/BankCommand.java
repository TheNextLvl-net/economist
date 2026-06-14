package net.thenextlvl.economist.plugin.command.bank;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.brigadier.BrigadierCommand;

public final class BankCommand extends BrigadierCommand {
    private BankCommand(final EconomistPlugin plugin) {
        super(plugin, "bank", "economist.bank");
    }

    public static LiteralCommandNode<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new BankCommand(plugin);
        return command.create()
                .then(BankBalanceCommand.create(plugin))
                .then(BankCreateCommand.create(plugin))
                .then(BankDeleteCommand.create(plugin))
                .then(BankDepositCommand.create(plugin))
                .then(BankInfoCommand.create(plugin))
                .then(BankListCommand.create(plugin))
                .then(BankMembersCommand.create(plugin))
                .then(BankMovementsCommand.create(plugin))
                .then(BankTopCommand.create(plugin))
                .then(BankTransferCommand.create(plugin))
                .then(BankWithdrawCommand.create(plugin))
                .build();
    }
}
