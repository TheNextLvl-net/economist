package net.thenextlvl.economist.plugin.controller.data;

import de.chojo.sadu.sqlite.databases.SqLite;
import net.thenextlvl.economist.plugin.EconomistPlugin;

import java.io.File;
import java.sql.SQLException;

public class SQLiteController extends SQLController {
    public SQLiteController(final EconomistPlugin plugin) throws SQLException {
        super(new DriverManagerDataSource("jdbc:sqlite:" + new File(plugin.getDataFolder(), "saves.db")),
                plugin, DatabaseDialect.SQLITE, SqLite.get());
    }
}
