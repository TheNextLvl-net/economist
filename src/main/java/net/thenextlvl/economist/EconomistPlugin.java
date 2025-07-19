package net.thenextlvl.economist;

import core.file.format.GsonFile;
import core.i18n.file.ComponentBundle;
import core.io.IO;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.key.Key;
import net.thenextlvl.economist.api.EconomyController;
import net.thenextlvl.economist.api.bank.BankController;
import net.thenextlvl.economist.command.AccountCommand;
import net.thenextlvl.economist.command.BalanceCommand;
import net.thenextlvl.economist.command.BalanceTopCommand;
import net.thenextlvl.economist.command.EconomyCommand;
import net.thenextlvl.economist.command.PayCommand;
import net.thenextlvl.economist.configuration.PluginConfig;
import net.thenextlvl.economist.controller.EconomistBankController;
import net.thenextlvl.economist.controller.EconomistEconomyController;
import net.thenextlvl.economist.controller.data.DataController;
import net.thenextlvl.economist.controller.data.SQLiteController;
import net.thenextlvl.economist.listener.ConnectionListener;
import net.thenextlvl.economist.model.EconomistCurrencyHolder;
import net.thenextlvl.economist.service.BankControllerDelegate;
import net.thenextlvl.economist.service.EconomyControllerDelegate;
import net.thenextlvl.economist.version.PluginVersionChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

@NullMarked
public class EconomistPlugin extends JavaPlugin {
    private final PluginVersionChecker versionChecker = new PluginVersionChecker(this);
    private final Metrics metrics = new Metrics(this, 23261);

    public final PluginConfig config = new GsonFile<>(
            IO.of(getDataFolder(), "config.json"),
            new PluginConfig()
    ).validate().save().getRoot();

    private final Key abbreviationsKey = Key.key("economist", "translations");
    private final Key translationsKey = Key.key("economist", "translations");
    private final Path translations = getDataPath().resolve("translations");

    private final ComponentBundle bundle = ComponentBundle.builder(translationsKey, translations)
            .placeholder("prefix", "prefix")
            .resource("economist.properties", Locale.US)
            .resource("economist_german.properties", Locale.GERMANY)
            .build();

    private final ComponentBundle abbreviations = ComponentBundle.builder(abbreviationsKey, translations)
            .resource("abbreviations.properties", Locale.US)
            .resource("abbreviations_german.properties", Locale.GERMANY)
            .build();

    private final EconomistBankController bankController = new EconomistBankController(this);
    private final EconomistEconomyController economyController = new EconomistEconomyController(this);
    private final EconomistCurrencyHolder currencyHolder = new EconomistCurrencyHolder(this);
    private final DataController dataController;

    public EconomistPlugin() throws SQLException {
        this.dataController = switch (config.storageType) {
            case SQLite -> new SQLiteController(this);
            default -> throw new IllegalStateException("Unexpected value: " + config.storageType);
        };
    }

    @Override
    public void onLoad() {
        versionChecker.checkVersion();
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
        metrics.shutdown();
    }

    private void registerServices() {
        var services = getServer().getServicesManager();
        if (config.banks.enabled)
            services.register(BankController.class, bankController, this, ServicePriority.Highest);
        services.register(EconomyController.class, economyController, this, ServicePriority.Highest);

        if (getServer().getPluginManager().getPlugin("ServiceIO") == null) return;
        var economy = new EconomyControllerDelegate(this);
        var banks = config.banks.enabled ? new BankControllerDelegate(economy, this) : null;
        economy.register(banks);
        getComponentLogger().info("Registered ServiceIO support");
    }

    private void registerCommands() {
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
            // if (config().banks().enabled()) {
            //     event.registrar().register(BankCommand.create(this), "Manage user accounts");
            // }
            event.registrar().register(AccountCommand.create(this), "Manage user accounts");
            event.registrar().register(BalanceCommand.create(this), "Display a players balance", config.balanceAliases);
            event.registrar().register(EconomyCommand.create(this), "Manage the economy", List.of("eco"));
            event.registrar().register(PayCommand.create(this), "Pay another player");
            event.registrar().register(BalanceTopCommand.create(this), "Shows the balance top-list", List.of("baltop"));
        }));
    }

    public ComponentBundle abbreviations() {
        return abbreviations;
    }

    public ComponentBundle bundle() {
        return bundle;
    }

    public EconomistBankController bankController() {
        return bankController;
    }

    public EconomistCurrencyHolder currencyHolder() {
        return currencyHolder;
    }

    public EconomistEconomyController economyController() {
        return economyController;
    }

    public DataController dataController() {
        return dataController;
    }
}
