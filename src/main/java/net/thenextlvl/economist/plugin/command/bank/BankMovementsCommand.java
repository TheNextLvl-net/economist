package net.thenextlvl.economist.plugin.command.bank;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;

final class BankMovementsCommand extends SimpleCommand {
    private BankMovementsCommand(final EconomistPlugin plugin) {
        super(plugin, "movements", "economist.bank.movements");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new BankMovementsCommand(plugin);
        return command.create().executes(command);
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        // todo: implement
        return SINGLE_SUCCESS;
    }
}
