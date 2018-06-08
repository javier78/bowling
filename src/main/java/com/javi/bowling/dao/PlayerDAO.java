package com.javi.bowling.dao;

import com.javi.bowling.model.DatabaseUtil;
import com.javi.bowling.model.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerDAO implements IDAO<Player> {

    private Connection conn;

    public PlayerDAO(){};

    /* package-private */ PlayerDAO(Connection connection) {
        conn = connection;
    }

    @Override
    public List<Player> findAll() {
        List<Player> players = new ArrayList<>();
        if(conn == null) {
            conn = DatabaseUtil.connect();
        }
        try {
            String sql = "SELECT * FROM Players;";
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery(sql);
            while(resultSet.next()) {
                Player player = new Player();
                int id = resultSet.getInt("id");
                player.setId(id);
                players.add(player);
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return players;
    }

    @Override
    public Player findById(int id) {
        Player player = new Player();

        if(conn == null) {
            conn = DatabaseUtil.connect();
        }
        try {
            String sql = "SELECT * FROM Players WHERE Players.id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            player.setId(resultSet.getInt("id"));
            player.setName(resultSet.getString("name"));
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return player;
    }

    @Override
    public int insert(Player row) {
        return 0;
    }

    @Override
    public boolean update(Player row) {
        return false;
    }

}
