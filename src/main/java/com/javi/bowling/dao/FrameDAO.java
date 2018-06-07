package com.javi.bowling.dao;

import com.javi.bowling.enums.FrameType;
import com.javi.bowling.model.DatabaseUtil;
import com.javi.bowling.model.Frame;
import com.javi.bowling.model.Game;
import com.javi.bowling.model.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FrameDAO implements IDAO<Frame> {
    private Connection conn;

    public FrameDAO() {}

    /**
     * This constructor is only meant to be used for testing!
     * @param connection A mocked connection object.
     */
    /* package-private */ FrameDAO(Connection connection) {
        conn = connection;
    }

    @Override
    public List<Frame> findAll() {
        List<Frame> frames = new ArrayList<>();
        if(conn == null) {
            conn = DatabaseUtil.connect();
        }
        try {
            String sql = "SELECT * FROM Frames;";
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery(sql);
            while(resultSet.next()) {
                Frame frame = new Frame();
                int id = resultSet.getInt("id");
                frame.setId(id);

                PlayerDAO playerDAO = new PlayerDAO();
                Player player = playerDAO.findById(resultSet.getInt("player_id"));

                GameDAO gameDAO = new GameDAO();
                Game game = gameDAO.findById(resultSet.getInt("game_id"));

                frame.setPlayer(player);
                frame.setGame(game);
                frame.setFrameNumber(resultSet.getInt("frame_number"));
                frame.setFrameType(FrameType.getTypeByName(resultSet.getString("frame_type")));
                frames.add(frame);
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return frames;
    }

    @Override
    public Frame findById(int id) {
        Frame frame = new Frame();
        if(conn == null) {
            conn = DatabaseUtil.connect();
        }
        try {
            String sql = "SELECT * FROM Frames WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            frame.setId(resultSet.getInt("id"));

            PlayerDAO playerDAO = new PlayerDAO();
            Player player = playerDAO.findById(resultSet.getInt("player_id"));

            GameDAO gameDAO = new GameDAO();
            Game game = gameDAO.findById(resultSet.getInt("game_id"));

            frame.setPlayer(player);
            frame.setGame(game);
            frame.setFrameNumber(resultSet.getInt("frame_number"));
            frame.setFrameType(FrameType.getTypeByName(resultSet.getString("frame_type")));

            if(resultSet.next()) {
                System.out.println("This shouldn't return more than 1 record.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return frame;
    }
}
