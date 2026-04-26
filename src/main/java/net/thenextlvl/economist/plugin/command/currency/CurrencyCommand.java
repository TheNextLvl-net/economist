package net.thenextlvl.economist.plugin.command.currency;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.brigadier.BrigadierCommand;

public final class CurrencyCommand extends BrigadierCommand {
    private CurrencyCommand(final EconomistPlugin plugin) {
        super(plugin, "currency", "economist.currency");
    }

    public static LiteralCommandNode<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new CurrencyCommand(plugin);
        return command.create()
                .then(CurrencyCreateCommand.create(plugin))
                .then(CurrencyDefaultCommand.create(plugin))
                .then(CurrencyDeleteCommand.create(plugin))
                .then(CurrencyDisplayNameCommand.create(plugin))
                .then(CurrencyFractionalDigitsCommand.create(plugin))
                .then(CurrencyInfoCommand.create(plugin))
                .then(CurrencyListCommand.create(plugin))
                .then(CurrencyMaxBalanceCommand.create(plugin))
                .then(CurrencyMinBalanceCommand.create(plugin))
                .then(CurrencySymbolCommand.create(plugin))
                .build();
    }
}
