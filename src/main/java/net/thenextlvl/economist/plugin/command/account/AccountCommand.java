package net.thenextlvl.economist.plugin.command.account;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.BalanceCommand;
import net.thenextlvl.economist.plugin.command.brigadier.BrigadierCommand;

public final class AccountCommand extends BrigadierCommand {
    private AccountCommand(final EconomistPlugin plugin) {
        super(plugin, "account", "economist.account");
    }

    public static LiteralCommandNode<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new AccountCommand(plugin);
        return command.create()
                .then(BalanceCommand.create(plugin))
                .then(AccountCreateCommand.create(plugin))
                .then(AccountDeleteCommand.create(plugin))
                .then(AccountPruneCommand.create(plugin))
                .build();
    }
}
