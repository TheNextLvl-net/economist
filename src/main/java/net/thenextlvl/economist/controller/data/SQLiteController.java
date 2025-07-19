package net.thenextlvl.economist.controller.data;

import net.thenextlvl.economist.EconomistPlugin;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;

@NullMarked
public class SQLiteController extends SQLController {
    private static final String LIST_ACCOUNT_OWNERS = statement("sql/query/account_owners.sql");
    private static final String DELETE_ACCOUNTS = statement("sql/update/delete_accounts.sql");

    public SQLiteController(EconomistPlugin plugin) throws SQLException {
        super(DriverManager.getConnection("jdbc:sqlite:" + new File(plugin.getDataFolder(), "saves.db")), plugin);
    }

    @Override
    public int prune(Duration duration, @Nullable World world) {
        return 0;
    }

    @Override
    protected void setupDatabase() throws SQLException {
        executeUpdate(statement("sql/table/accounts.sql"));
        executeUpdate(statement("sql/table/banks.sql"));
        executeUpdate(statement("sql/trigger/enforce_unique_uuid_world_insert.sql"));
        executeUpdate(statement("sql/trigger/enforce_unique_uuid_world_update.sql"));
    }
}
