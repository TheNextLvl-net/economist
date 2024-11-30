package net.thenextlvl.economist.controller.data;

import net.thenextlvl.economist.EconomistPlugin;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;

@NullMarked
public class SQLiteController extends SQLController {
    public SQLiteController(EconomistPlugin plugin) throws SQLException {
        super(DriverManager.getConnection("jdbc:sqlite:" + new File(plugin.getDataFolder(), "saves.db")), plugin);
    }
}
