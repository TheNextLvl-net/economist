package net.thenextlvl.economist.plugin.controller.data;

import de.chojo.sadu.core.jdbc.JdbcConfig;

final class RemoteJdbcConfig extends JdbcConfig<RemoteJdbcConfig> {
    private final String driver;
    private final String baseUrl;
    private final String defaultDriverClass;

    RemoteJdbcConfig(final String driver, final String baseUrl, final String defaultDriverClass) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.defaultDriverClass = defaultDriverClass;
    }

    @Override
    protected String defaultDriverClass() {
        return defaultDriverClass;
    }

    @Override
    protected String driver() {
        return driver;
    }

    @Override
    protected String baseUrl() {
        return baseUrl;
    }
}
