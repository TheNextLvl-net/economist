package net.thenextlvl.economist.controller.data;

import net.thenextlvl.economist.EconomistPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;

public abstract class SQLController implements AutoCloseable, DataController {
    private final @NonNull Connection connection;
    protected final @NonNull EconomistPlugin plugin;

    public SQLController(@NonNull Connection connection, @NonNull EconomistPlugin plugin) throws SQLException {
        this.connection = connection;
        this.plugin = plugin;
        setupDatabase();
        migrateDatabase();
    }

    protected abstract void setupDatabase() throws SQLException;
    
    protected void migrateDatabase() throws SQLException {
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    protected <T> T executeQuery(@NonNull String sql, @NonNull ThrowingFunction<ResultSet, T> mapper, Object... parameters) throws SQLException {
        try (var preparedStatement = connection.prepareStatement(sql)) {
            for (var i = 0; i < parameters.length; i++)
                preparedStatement.setObject(i + 1, parameters[i]);
            try (var resultSet = preparedStatement.executeQuery()) {
                return mapper.apply(resultSet);
            }
        }
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    protected int[] executeBatchUpdate(String sql, Iterable<Object[]> batches) throws SQLException {
        try (var preparedStatement = connection.prepareStatement(sql)) {
            for (var parameters : batches) {
                for (var i = 0; i < parameters.length; i++) preparedStatement.setObject(i + 1, parameters[i]);
                preparedStatement.addBatch();
            }
            return preparedStatement.executeBatch();
        }
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    protected int executeUpdate(@NonNull String sql, Object... parameters) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            for (var i = 0; i < parameters.length; i++) statement.setObject(i + 1, parameters[i]);
            return statement.executeUpdate();
        }
    }

    protected static @NonNull String statement(@NonNull String file) {
        try (var resource = SQLController.class.getClassLoader().getResourceAsStream(file)) {
            if (resource == null) throw new FileNotFoundException("Resource not found: " + file);
            try (var reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @FunctionalInterface
    protected interface ThrowingFunction<T, R> {
        @Nullable
        R apply(@NonNull T t) throws SQLException;
    }
}
