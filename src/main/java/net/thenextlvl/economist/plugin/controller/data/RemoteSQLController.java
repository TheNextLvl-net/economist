package net.thenextlvl.economist.plugin.controller.data;

import net.thenextlvl.economist.plugin.EconomistPlugin;

import javax.sql.DataSource;
import java.sql.SQLException;

public class RemoteSQLController extends SQLController {
    public RemoteSQLController(final DataSource dataSource, final EconomistPlugin plugin, final DatabaseDialect dialect) throws SQLException {
        super(dataSource, plugin, dialect, dialect.database());
    }
}
