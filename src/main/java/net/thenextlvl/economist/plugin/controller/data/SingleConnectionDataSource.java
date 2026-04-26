package net.thenextlvl.economist.plugin.controller.data;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.logging.Logger;

final class SingleConnectionDataSource implements DataSource {
    private final Connection delegate;

    SingleConnectionDataSource(final Connection delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public Connection getConnection() {
        return suppressClose(delegate);
    }

    @Override
    public Connection getConnection(final String username, final String password) {
        return getConnection();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(final PrintWriter out) {
    }

    @Override
    public void setLoginTimeout(final int seconds) {
    }

    @Override
    public int getLoginTimeout() {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Single connection data source does not expose a parent logger");
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) return iface.cast(this);
        throw new SQLException("Not a wrapper for " + iface.getName());
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) {
        return iface.isInstance(this);
    }

    private static Connection suppressClose(final Connection connection) {
        final InvocationHandler handler = (proxy, method, args) -> {
            if (method.getName().equals("close")) return null;
            if (method.getName().equals("isClosed")) return connection.isClosed();
            return method.invoke(connection, args);
        };
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                handler
        );
    }
}
