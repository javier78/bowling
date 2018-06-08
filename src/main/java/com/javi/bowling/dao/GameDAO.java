package com.javi.bowling.dao;

import com.javi.bowling.model.DatabaseUtil;
import com.javi.bowling.model.Game;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDAO implements IDAO<Game> {

    private Connection conn;

    public GameDAO(){}

    /**
     * This constructor is only meant to be used for testing!
     * @param connection A mocked connection object.
     */
    /* package-private */ GameDAO(Connection connection) {
        conn = connection;
    }

    public List<Game> findAll() {
        List<Game> games = new ArrayList<>();
        if(conn == null) {
            conn = DatabaseUtil.connect();
        }
        try {
            String sql = "SELECT * FROM Games;";
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery(sql);
            while(resultSet.next()) {
                Game game = new Game();
                int id = resultSet.getInt("id");
                game.setId(id);
                games.add(game);
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return games;
    }

    public Game findById(int id) {
        Game game = new Game();
        if(conn == null) {
            conn = DatabaseUtil.connect();
        }
        try {
            String sql = "SELECT * FROM Games WHERE Games.id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            game.setId(resultSet.getInt("id"));
            if(resultSet.next()) {
                System.out.println("This shouldn't return more than 1 record");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return game;
    }

    /**
     * Utility method to facilitate creating a game
     * @return A Gam object with its id field set.
     */
    public Game initiateNewGame() {
        Game game = new Game();
        int id = insert(game);
        game.setId(id);
        return game;
    }

    @Override
    public int insert(Game row) {
        Connection conn = DatabaseUtil.connect();
        int generatedKey = 0;
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO Games (id) VALUES(NULL)", Statement.RETURN_GENERATED_KEYS);
            stmt.execute();
            ResultSet resultSet = stmt.getGeneratedKeys();
            if(resultSet.next()) {
                generatedKey = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return generatedKey;
    }

    @Override
    public boolean update(Game row) {
        return false;
    }
}
