package net.thenextlvl.economist;

import core.file.format.GsonFile;
import core.i18n.file.ComponentBundle;
import core.io.IO;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.thenextlvl.economist.api.EconomyController;
import net.thenextlvl.economist.api.bank.BankController;
import net.thenextlvl.economist.command.BalanceCommand;
import net.thenextlvl.economist.command.BankCommand;
import net.thenextlvl.economist.command.EconomyCommand;
import net.thenextlvl.economist.command.TopListCommand;
import net.thenextlvl.economist.controller.EconomistBankController;
import net.thenextlvl.economist.controller.EconomistEconomyController;
import net.thenextlvl.economist.model.configuration.PluginConfig;
import net.thenextlvl.economist.model.configuration.StorageType;
import net.thenextlvl.economist.version.PluginVersionChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.math.BigDecimal;
import java.util.Locale;

@Getter
@Accessors(fluent = true)
public class EconomistPlugin extends JavaPlugin {
    private final PluginVersionChecker versionChecker = new PluginVersionChecker(this);
    private final Metrics metrics = new Metrics(this, 23261);

    private final PluginConfig config = new GsonFile<>(
            IO.of(getDataFolder(), "config.json"),
            new PluginConfig(2, "$", new BigDecimal(0), StorageType.SQLite)
    ).validate().save().getRoot();

    private final ComponentBundle bundle = new ComponentBundle(
            new File(getDataFolder(), "translations"),
            audience -> audience instanceof Player player ? player.locale() : Locale.US)
            .register("economist", Locale.US)
            .register("economist_german", Locale.GERMANY)
            .miniMessage(bundle -> MiniMessage.builder().tags(TagResolver.resolver(
                    TagResolver.standard(),
                    Placeholder.component("prefix", bundle.component(Locale.US, "prefix"))
            )).build());

    private final BankController bankController = new EconomistBankController(this);
    private final EconomyController economyController;

    public EconomistPlugin() {
        this.economyController = new EconomistEconomyController(this);
    }

    @Override
    public void onLoad() {
        versionChecker().checkVersion();
        registerCommands();
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        metrics().shutdown();
    }

    private void registerCommands() {
        new BalanceCommand(this).register();
        new BankCommand(this).register();
        new EconomyCommand(this).register();
        new TopListCommand(this).register();
    }
}
