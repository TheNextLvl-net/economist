package net.thenextlvl.economist.plugin.command.bank;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.economist.bank.Bank;
import net.thenextlvl.economist.currency.Currency;
import net.thenextlvl.economist.plugin.EconomistPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

final class BankSupport {
    static final String OWNER_ARGUMENT = "owner";
    static final String NAME_ARGUMENT = "name";
    static final String BANK_ARGUMENT = "bank";
    static final String TARGET_ARGUMENT = "target";
    static final int PAGE_SIZE = 10;

    private BankSupport() {
    }

    static Currency currency(final EconomistPlugin plugin) {
        return plugin.currencyController().getDefaultCurrency();
    }

    static CompletableFuture<Optional<Bank>> resolveBankTarget(final EconomistPlugin plugin,
                                                               final CommandContext<CommandSourceStack> context) {
        return findArgument(context, OWNER_ARGUMENT, OfflinePlayer.class)
                .map(plugin.bankController()::resolveBank)
                .orElseGet(() -> findArgument(context, NAME_ARGUMENT, String.class)
                        .map(plugin.bankController()::resolveBank)
                        .orElseGet(() -> senderPlayer(context)
                                .map(plugin.bankController()::resolveBank)
                                .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()))));
    }

    static CompletableFuture<Optional<Bank>> resolveMemberBank(final EconomistPlugin plugin,
                                                               final CommandContext<CommandSourceStack> context) {
        return findArgument(context, BANK_ARGUMENT, String.class)
                .map(plugin.bankController()::resolveBank)
                .orElseGet(() -> resolveBankTarget(plugin, context));
    }

    static <T> Optional<T> findArgument(final CommandContext<CommandSourceStack> context,
                                        final String name, final Class<T> type) {
        try {
            return Optional.of(context.getArgument(name, type));
        } catch (final IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    static Optional<Player> senderPlayer(final CommandContext<CommandSourceStack> context) {
        return context.getSource().getSender() instanceof final Player player ? Optional.of(player) : Optional.empty();
    }

    static void sendBankNotFound(final EconomistPlugin plugin, final CommandSender sender,
                                 final CommandContext<CommandSourceStack> context) {
        final var owner = findArgument(context, OWNER_ARGUMENT, OfflinePlayer.class).orElse(null);
        final var name = findArgument(context, NAME_ARGUMENT, String.class).orElse(null);
        if (owner != null) {
            plugin.bundle().sendMessage(sender, "bank.not-found.other",
                    Placeholder.parsed("owner", playerName(owner)));
            return;
        }
        if (name != null) {
            plugin.bundle().sendMessage(sender, "bank.not-found.name", Placeholder.parsed("bank", name));
            return;
        }
        plugin.bundle().sendMessage(sender, sender instanceof Player ? "bank.not-found.self" : "player.define");
    }

    static boolean canManageBank(final CommandSender sender, final CommandContext<CommandSourceStack> context,
                                 final Bank bank) {
        if (sender instanceof final Player player && bank.getOwner().equals(player.getUniqueId())) {
            return true;
        }
        return findArgument(context, BANK_ARGUMENT, String.class).isPresent()
                && sender.hasPermission("economist.bank.manage.others");
    }

    static String playerName(final OfflinePlayer player) {
        return player.getName() != null ? player.getName() : player.getUniqueId().toString();
    }
}
