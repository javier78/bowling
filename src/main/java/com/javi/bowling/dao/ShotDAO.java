package com.javi.bowling.dao;

import com.javi.bowling.model.DatabaseUtil;
import com.javi.bowling.model.Frame;
import com.javi.bowling.model.Shot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShotDAO implements IDAO<Shot> {

    private Connection conn;

    /**
     * This constructor is only meant to be used for testing!
     * @param connection A mocked connection object.
     */
    /* package-private */ ShotDAO(Connection connection) {
        conn = connection;
    }

    @Override
    public List<Shot> findAll() {
        List<Shot> shots = new ArrayList<>();
        if(conn == null) {
            conn = DatabaseUtil.connect();
        }
        try {
            String sql = "SELECT * FROM Shots";
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery(sql);
            while(resultSet.next()) {
                Shot shot = new Shot();
                int id = resultSet.getInt("id");

                FrameDAO dao = new FrameDAO();
                Frame frame = dao.findById(resultSet.getInt("frame_id"));

                shot.setId(id);
                shot.setFrame(frame);
                shot.setShotNumber(resultSet.getInt("shot_number"));
                shot.setShotValue(resultSet.getInt("shot_value"));
                shots.add(shot);
            }
            conn.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return shots;
    }

    @Override
    public Shot findById(int id) {
        Shot shot = new Shot();
        if(conn == null) {
            conn = DatabaseUtil.connect();
        }
        try {
            String sql = "SELECT * FROM Shots WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();

            FrameDAO frameDAO = new FrameDAO();
            Frame frame = frameDAO.findById(resultSet.getInt("frame_id"));

            shot.setId(resultSet.getInt("id"));
            shot.setFrame(frame);
            shot.setShotValue(resultSet.getInt("shot_value"));
            shot.setShotNumber(resultSet.getInt("shot_number"));

            if(resultSet.next()) {
                System.out.println("This shouldn't return more than 1 record.");
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shot;
    }
}
