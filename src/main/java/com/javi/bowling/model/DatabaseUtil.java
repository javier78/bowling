package com.javi.bowling.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {
    public static Connection connect() {
        Connection conn = null;
        try {
            // model parameters
            String url = "jdbc:sqlite:bowling.model";
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    /**
     * Should only be called once, and by the Application class on startup
     */
    public static void initDB() {
        Connection conn = connect();
        String createSqlString = "CREATE TABLE IF NOT EXISTS `Shots` (" +
                "    `id`    INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "    `frame_id`    INTEGER NOT NULL," +
                "    `shot_number`    INTEGER NOT NULL," +
                "    `shot_value`    INTEGER," +
                "    FOREIGN KEY(`frame_id`) REFERENCES Frames(id)" +
                ");" +
                "CREATE TABLE IF NOT EXISTS `Players` (" +
                "    `id`    INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "    `name`    TEXT" +
                ");" +
                "CREATE TABLE IF NOT EXISTS `Games` (" +
                "    `id`    INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT" +
                ");" +
                "CREATE TABLE IF NOT EXISTS `Game_Player` (" +
                "    `id`    INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "    `game_id`    INTEGER NOT NULL," +
                "    `player_id`    INTEGER NOT NULL," +
                "    FOREIGN KEY(`game_id`) REFERENCES Games(id)," +
                "    FOREIGN KEY(`player_id`) REFERENCES `Players`(`id`)" +
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
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(createSqlString);
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
