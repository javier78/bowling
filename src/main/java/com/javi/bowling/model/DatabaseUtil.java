package com.javi.bowling.model;

import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {
    public static Connection connect() throws SQLException {
        Connection conn;
        // model parameters
        String url = "jdbc:sqlite:bowling.sqlite";
        // create a connection to the database
        conn = DriverManager.getConnection(url);

        return conn;
    }

    /**
     * Should only be called once, and by the Application class on startup
     */
    public static void initDB() {
        Connection conn = null;
        Statement statement = null;
        try {
            String createSqlString = "CREATE TABLE IF NOT EXISTS `Shots` (" +
                    "    `id`    INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "    `frame_id`    INTEGER NOT NULL," +
                    "    `shot_number`    INTEGER NOT NULL," +
                    "    `shot_value`    INTEGER," +
                    "    FOREIGN KEY(`frame_id`) REFERENCES Frames(id)" +
                    ");" +
                    "CREATE TABLE IF NOT EXISTS `Players` (" +
                    "    `id`    INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "    `name`    TEXT," +
                    "    `game_id`    INTEGER NOT NULL REFERENCES Games(id)" +
                    ");" +
                    "CREATE TABLE IF NOT EXISTS `Games` (" +
                    "    `id`    INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT" +
                    ");" +
                    "CREATE TABLE IF NOT EXISTS `Frames` (" +
                    "    `id`    INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "    `game_id`    INTEGER NOT NULL," +
                    "    `player_id`    INTEGER NOT NULL," +
                    "    `frame_number`    INTEGER NOT NULL," +
                    "    `frame_type`    TEXT NOT NULL," +
                    "    FOREIGN KEY(`game_id`) REFERENCES Games(id)," +
                    "    FOREIGN KEY(`player_id`) REFERENCES Players(id)" +
                    ");";
            conn = connect();
            statement = conn.createStatement();
            statement.executeUpdate(createSqlString);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(conn);
        }
    }
}
