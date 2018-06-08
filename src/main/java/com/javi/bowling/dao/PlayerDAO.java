package com.javi.bowling.dao;

import com.javi.bowling.exception.GameAlreadyStartedException;
import com.javi.bowling.model.DatabaseUtil;
import com.javi.bowling.model.Game;
import com.javi.bowling.model.Player;
import org.apache.commons.dbutils.DbUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerDAO implements IDAO<Player> {

    @Override
    public List<Player> findAll() {
        List<Player> players = new ArrayList<>();
        Connection conn = null;
        Statement statement = null;
        try {
            String sql = "SELECT * FROM Players;";
            conn = DatabaseUtil.connect();
            statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while(resultSet.next()) {
                Player player = new Player();
                int id = resultSet.getInt("id");
                player.setId(id);
                players.add(player);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(conn);
        }
        return players;
    }

    @Override
    public Player findById(int id) {
        Player player = new Player();
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            String sql = "SELECT * FROM Players WHERE Players.id = ?";
            conn = DatabaseUtil.connect();
            statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            player.setId(resultSet.getInt("id"));
            player.setName(resultSet.getString("name"));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(conn);
        }
        return player;
    }

    public Player createPlayer(String name, int gameId) throws GameAlreadyStartedException {
        GameDAO gameDAO = new GameDAO();
        Game game = gameDAO.findById(gameId);
        if(!gameDAO.isGameStarted(game)) {
            Player player = new Player();
            player.setName(name);
            player.setGame(game);

            int id = insert(player);
            player.setId(id);
            return player;
        } else {
            throw new GameAlreadyStartedException("Cannot add players to a game that has already begun.");
        }
    }

    @Override
    public int insert(Player row) {
        Connection conn = null;
        PreparedStatement statement = null;
        int generatedKey = 0;
        try {
            conn = DatabaseUtil.connect();
            statement = conn.prepareStatement("INSERT INTO Players (`name`, `game_id`) VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, row.getName());
            statement.setInt(2, row.getGame().getId());
            statement.execute();
            ResultSet resultSet = statement.getGeneratedKeys();
            if(resultSet.next()) {
                generatedKey = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(conn);
        }
        return generatedKey;
    }

    @Override
    public boolean update(Player row) {
        return false;
    }

}
