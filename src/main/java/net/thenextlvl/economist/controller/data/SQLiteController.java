package net.thenextlvl.economist.controller.data;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteController extends SQLController {
    public SQLiteController(File file) throws SQLException {
        super(DriverManager.getConnection("jdbc:sqlite:" + file));
    }
}
