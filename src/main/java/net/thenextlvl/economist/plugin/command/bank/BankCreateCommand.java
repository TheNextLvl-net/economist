package net.thenextlvl.economist.plugin.command.bank;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import core.paper.brigadier.arguments.OfflinePlayerArgumentType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import net.thenextlvl.economist.plugin.command.brigadier.SimpleCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

final class BankCreateCommand extends SimpleCommand {
    private BankCreateCommand(final EconomistPlugin plugin) {
        super(plugin, "create", "economist.bank.create");
    }

    static LiteralArgumentBuilder<CommandSourceStack> create(final EconomistPlugin plugin) {
        final var command = new BankCreateCommand(plugin);
        final var name = Commands.argument("name", StringArgumentType.word());
        final var owner = Commands.argument("owner", OfflinePlayerArgumentType.player())
                .requires(stack -> stack.getSender().hasPermission("economist.bank.create.others"));
        return command.create()
                .then(name.executes(command))
                .then(owner.then(name.executes(command)));
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        final var name = context.getArgument("name", String.class);
        final var owner = tryGetArgument(context, "owner", OfflinePlayer.class)
                .orElseGet(() -> sender instanceof final Player player ? player : null);
        if (owner == null) {
            plugin.bundle().sendMessage(sender, "player.define");
            return 0;
        }

        plugin.bankController().createBank(owner, name).thenAccept(bank -> {
            final var self = sender instanceof final Player player && owner.getUniqueId().equals(player.getUniqueId());
            plugin.bundle().sendMessage(sender, self ? "bank.created.self" : "bank.created.other",
                    Placeholder.parsed("bank", bank.getName()),
                    Placeholder.parsed("owner", BankSupport.playerName(owner)));
        }).exceptionally(throwable -> {
            final var self = sender instanceof final Player player && owner.getUniqueId().equals(player.getUniqueId());
            plugin.bundle().sendMessage(sender, self ? "bank.exists.self" : "bank.exists.other",
                    Placeholder.parsed("bank", name),
                    Placeholder.parsed("owner", BankSupport.playerName(owner)));
            return null;
        });
        return SINGLE_SUCCESS;
    }
}
