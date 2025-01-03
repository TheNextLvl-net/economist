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
import net.thenextlvl.economist.command.AccountCommand;
import net.thenextlvl.economist.command.BalanceCommand;
import net.thenextlvl.economist.command.EconomyCommand;
import net.thenextlvl.economist.command.PayCommand;
import net.thenextlvl.economist.command.BalanceTopCommand;
import net.thenextlvl.economist.configuration.PluginConfig;
import net.thenextlvl.economist.controller.EconomistBankController;
import net.thenextlvl.economist.controller.EconomistEconomyController;
import net.thenextlvl.economist.controller.data.DataController;
import net.thenextlvl.economist.controller.data.SQLiteController;
import net.thenextlvl.economist.listener.ConnectionListener;
import net.thenextlvl.economist.service.ServiceBankController;
import net.thenextlvl.economist.service.ServiceEconomyController;
import net.thenextlvl.economist.version.PluginVersionChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.sql.SQLException;
import java.util.Locale;

@Getter
@NullMarked
@Accessors(fluent = true)
public class EconomistPlugin extends JavaPlugin {
    private final PluginVersionChecker versionChecker = new PluginVersionChecker(this);
    private final Metrics metrics = new Metrics(this, 23261);

    private final PluginConfig config = new GsonFile<>(
            IO.of(getDataFolder(), "config.json"),
            new PluginConfig()
    ).validate().save().getRoot();

    private final File translations = new File(getDataFolder(), "translations");

    private final ComponentBundle bundle = new ComponentBundle(translations,
            audience -> audience instanceof Player player ? player.locale() : Locale.US)
            .register("economist", Locale.US)
            .register("economist_german", Locale.GERMANY)
            .miniMessage(bundle -> MiniMessage.builder().tags(TagResolver.resolver(
                    TagResolver.standard(),
                    Placeholder.component("prefix", bundle.component(Locale.US, "prefix"))
            )).build());

    private final ComponentBundle abbreviations = new ComponentBundle(translations,
            audience -> audience instanceof Player player ? player.locale() : Locale.US)
            .register("abbreviations", Locale.US)
            .register("abbreviations_german", Locale.GERMANY);

    private final EconomistBankController bankController = new EconomistBankController(this);
    private final EconomistEconomyController economyController = new EconomistEconomyController(this);
    private final DataController dataController;

    public EconomistPlugin() throws SQLException {
        this.dataController = switch (config().storageType()) {
            case SQLite -> new SQLiteController(this);
            default -> throw new IllegalStateException("Unexpected value: " + config().storageType());
        };
    }

    @Override
    public void onLoad() {
        versionChecker().checkVersion();
        registerServices();
        registerCommands();
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
    }

    @Override
    public void onDisable() {
        economyController().save();
        bankController().save();
        metrics().shutdown();
    }

    private void registerServices() {
        var services = getServer().getServicesManager();
        if (config().banks().enabled())
            services.register(BankController.class, bankController, this, ServicePriority.Highest);
        services.register(EconomyController.class, economyController, this, ServicePriority.Highest);

        if (getServer().getPluginManager().getPlugin("ServiceIO") == null) return;
        var banks = config().banks().enabled() ? new ServiceBankController(this) : null;
        new ServiceEconomyController(banks, this).register();
        getComponentLogger().info("Registered ServiceIO support");
    }

    private void registerCommands() {
        // if (config().banks().enabled()) new BankCommand(this).register();
        new AccountCommand(this).register();
        new BalanceCommand(this).register();
        new EconomyCommand(this).register();
        new PayCommand(this).register();
        new BalanceTopCommand(this).register();
    }
}
