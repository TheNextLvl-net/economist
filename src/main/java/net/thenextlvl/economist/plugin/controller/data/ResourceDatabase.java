package net.thenextlvl.economist.plugin.controller.data;

import de.chojo.sadu.core.databases.DefaultDatabase;
import de.chojo.sadu.core.updater.StatementSplitter;
import de.chojo.sadu.core.updater.UpdaterBuilder;
import de.chojo.sadu.updater.BaseSqlUpdaterBuilder;

final class ResourceDatabase implements DefaultDatabase<RemoteJdbcConfig, BaseSqlUpdaterBuilder<RemoteJdbcConfig, ?>> {
    private final String name;
    private final String[] aliases;
    private final String jdbcScheme;
    private final String driverClass;

    ResourceDatabase(final String name, final String jdbcScheme, final String driverClass, final String... aliases) {
        this.name = name;
        this.aliases = aliases;
        this.jdbcScheme = jdbcScheme;
        this.driverClass = driverClass;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String[] alias() {
        return aliases;
    }

    @Override
    public RemoteJdbcConfig jdbcBuilder() {
        return new RemoteJdbcConfig(name, jdbcScheme, driverClass);
    }

    @Override
    public String[] splitStatements(final String queries) {
        return StatementSplitter.split(queries);
    }

    @Override
    public UpdaterBuilder<RemoteJdbcConfig, BaseSqlUpdaterBuilder<RemoteJdbcConfig, ?>> newSqlUpdaterBuilder() {
        return new BaseSqlUpdaterBuilder<>(this);
    }
}
