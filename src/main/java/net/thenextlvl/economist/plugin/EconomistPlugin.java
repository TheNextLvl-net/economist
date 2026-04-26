package net.thenextlvl.economist.plugin;

import core.file.formats.GsonFile;
import dev.faststats.bukkit.BukkitMetrics;
import dev.faststats.core.ErrorTracker;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.key.Key;
import net.thenextlvl.economist.EconomyController;
import net.thenextlvl.economist.bank.BankController;
import net.thenextlvl.economist.currency.CurrencyController;
import net.thenextlvl.economist.plugin.command.BalanceCommand;
import net.thenextlvl.economist.plugin.command.BalanceTopCommand;
import net.thenextlvl.economist.plugin.command.EconomyCommand;
import net.thenextlvl.economist.plugin.command.PayCommand;
import net.thenextlvl.economist.plugin.command.account.AccountCommand;
import net.thenextlvl.economist.plugin.command.bank.BankCommand;
import net.thenextlvl.economist.plugin.command.currency.CurrencyCommand;
import net.thenextlvl.economist.plugin.configuration.PluginConfig;
import net.thenextlvl.economist.plugin.controller.EconomistBankController;
import net.thenextlvl.economist.plugin.controller.EconomistEconomyController;
import net.thenextlvl.economist.plugin.controller.data.DataController;
import net.thenextlvl.economist.plugin.controller.data.DataControllerFactory;
import net.thenextlvl.economist.plugin.currency.EconomistCurrencyController;
import net.thenextlvl.economist.plugin.listener.ConnectionListener;
import net.thenextlvl.economist.plugin.version.PluginVersionChecker;
import net.thenextlvl.i18n.ComponentBundle;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class EconomistPlugin extends JavaPlugin {
    public static final ErrorTracker ERROR_TRACKER = ErrorTracker.contextAware();

    private final PluginVersionChecker versionChecker = new PluginVersionChecker(this);

    private final BukkitMetrics fastStats = BukkitMetrics.factory()
            .errorTracker(ERROR_TRACKER)
            .token("39a650305577ef0e9c1580de408f48b0")
            .create(this);
    private final Metrics metrics = new Metrics(this, 23261);

    public final PluginConfig config = new GsonFile<>(
            getDataPath().resolve("config.json"),
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

    private final DataController dataController;
    private final EconomistCurrencyController currencyController;
    private final EconomistEconomyController economyController;
    private final EconomistBankController bankController;

    public EconomistPlugin() throws SQLException {
        this.currencyController = new EconomistCurrencyController(this);
        this.dataController = DataControllerFactory.create(this);
        this.economyController = new EconomistEconomyController(this);
        this.bankController = new EconomistBankController(this);
    }

    @Override
    public void onLoad() {
        versionChecker.checkVersion();
        currencyController().load(this);
        registerServices();
        registerCommands();
    }

    @Override
    public void onEnable() {
        fastStats.ready();
        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
    }

    @Override
    public void onDisable() {
        economyController().save();
        bankController().save();
        currencyController().save(this);
        fastStats.shutdown();
        metrics.shutdown();
    }

    private void registerServices() {
        final var services = getServer().getServicesManager();
        services.register(EconomyController.class, economyController, this, ServicePriority.Highest);
        services.register(BankController.class, bankController, this, ServicePriority.Highest);
        services.register(CurrencyController.class, currencyController, this, ServicePriority.Highest);
    }

    private void registerCommands() {
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
            event.registrar().register(AccountCommand.create(this), "Manage user accounts");
            event.registrar().register(BalanceCommand.create(this), "Display a players balance", config.balanceAliases);
            event.registrar().register(BankCommand.create(this), "Manage bank accounts");
            event.registrar().register(CurrencyCommand.create(this), "Manage currencies");
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

    public DataController dataController() {
        return dataController;
    }

    public EconomistEconomyController economyController() {
        return economyController;
    }

    public EconomistCurrencyController currencyController() {
        return currencyController;
    }

    public EconomistBankController bankController() {
        return bankController;
    }
}
