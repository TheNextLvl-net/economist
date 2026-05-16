package net.thenextlvl.economist.plugin.command.brigadier;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import org.jspecify.annotations.Nullable;

public abstract class SimpleCommand extends BrigadierCommand implements Command<CommandSourceStack> {
    protected SimpleCommand(final EconomistPlugin plugin, final String name, @Nullable final String permission) {
        super(plugin, name, permission);
    }
}
