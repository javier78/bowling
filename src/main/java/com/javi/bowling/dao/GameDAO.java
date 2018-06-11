package com.javi.bowling.dao;

import com.javi.bowling.model.DatabaseUtil;
import com.javi.bowling.model.Game;
import org.apache.commons.dbutils.DbUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDAO implements IDAO<Game> {

    public List<Game> findAll() {
        List<Game> games = new ArrayList<>();
        Connection conn = null;
        Statement statement = null;
        try {
            String sql = "SELECT * FROM Games;";
            conn = DatabaseUtil.connect();
            statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while(resultSet.next()) {
                Game game = new Game();
                int id = resultSet.getInt("id");
                game.setId(id);
                games.add(game);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(conn);
        }
        return games;
    }

    public Game findById(int id) {
        Game game = null;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            String sql = "SELECT * FROM Games WHERE Games.id = ?";
            conn = DatabaseUtil.connect();
            statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                game = new Game();
                game.setId(resultSet.getInt("id"));
            }
            if(resultSet.next()) {
                System.out.println("This shouldn't return more than 1 record");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(conn);
        }
        return game;
    }

    /**
     * Determines whether a game has started by checking whether frames are associated with it.
     * @param game the game to check
     * @return true if there are frames associated with the game id, false otherwise.
     */
    public boolean isGameStarted(Game game) {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            String sql = "SELECT COUNT(*) FROM Games JOIN Frames ON (Games.id = Frames.game_id) WHERE Games.id = ?";
            conn = DatabaseUtil.connect();
            statement = conn.prepareStatement(sql);
            statement.setInt(1, game.getId());
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);
            return count > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(conn);
        }
        return true;
    }

    /**
     * Utility method to facilitate creating a game
     * @return A Game object with its id field set.
     */
    public Game initiateNewGame() {
        Game game = new Game();
        int id = insert(game);
        game.setId(id);
        return game;
    }

    @Override
    public int insert(Game row) {
        Connection conn = null;
        PreparedStatement statement = null;
        int generatedKey = 0;
        try {
            conn = DatabaseUtil.connect();
            statement = conn.prepareStatement("INSERT INTO Games (id) VALUES(NULL)", Statement.RETURN_GENERATED_KEYS);
            statement.execute();
            ResultSet resultSet = statement.getGeneratedKeys();
            if(resultSet.next()) {
                generatedKey = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return generatedKey;
    }

    /**
     * A game can never be updated, so this will always return false
     * @param row The row object to be updated
     * @return false
     */
    @Override
    public boolean update(Game row) {
        return false;
    }
}
