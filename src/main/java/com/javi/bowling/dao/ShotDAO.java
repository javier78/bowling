package com.javi.bowling.dao;

import com.javi.bowling.model.DatabaseUtil;
import com.javi.bowling.model.Frame;
import com.javi.bowling.model.Shot;
import org.apache.commons.dbutils.DbUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShotDAO implements IDAO<Shot> {

    @Override
    public List<Shot> findAll() {
        List<Shot> shots = new ArrayList<>();
        Connection conn = null;
        Statement statement = null;
        try {
            conn = DatabaseUtil.connect();
            String sql = "SELECT * FROM Shots";
            statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
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
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(conn);
        }
        return shots;
    }

    @Override
    public Shot findById(int id) {
        Shot shot = null;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = DatabaseUtil.connect();
            String sql = "SELECT * FROM Shots WHERE id = ?";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();

            FrameDAO frameDAO = new FrameDAO();
            Frame frame = frameDAO.findById(resultSet.getInt("frame_id"));

            shot = new Shot();
            shot.setId(resultSet.getInt("id"));
            shot.setFrame(frame);
            shot.setShotValue(resultSet.getInt("shot_value"));
            shot.setShotNumber(resultSet.getInt("shot_number"));

            if(resultSet.next()) {
                System.out.println("This shouldn't return more than 1 record.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(conn);
        }
        return shot;
    }

    public Shot getPreviousShot(Shot shot) {
        if(shot.getShotNumber() > 1) {
            return findByFrameNumberAndShotNumber(shot.getFrame(), shot.getShotNumber() - 1);
        } else {
            return null;
        }
    }



    /**
     * Looks ahead and gets the next N shots, so a strike/spare may be calculated.
     * @param frame The starting point, the shots of this frame will also be returned.
     * @param shotsToReturn The number of shots to return.
     * @return A list of Shot objects. List size should always be equal to shotsToReturn, unless a lookahead frame is CURRENT
     */
    public List<Shot> getNextNShots(Frame frame, int shotsToReturn) {
        Connection conn = null;
        PreparedStatement statement = null;
        List<Shot> shots = new ArrayList<>();
        try {
            conn = DatabaseUtil.connect();
            String sql = "SELECT Shots.* FROM Frames " +
                    "JOIN Shots ON (Shots.frame_id = Frames.id) " +
                    "WHERE frame_number >= ? ORDER BY frame_number, shot_number LIMIT ?";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, frame.getFrameNumber());
            statement.setInt(2, shotsToReturn);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Shot shot = new Shot();
                shot.setId(resultSet.getInt("id"));
                shot.setShotNumber(resultSet.getInt("shot_number"));
                shot.setShotValue(resultSet.getInt("shot_value"));
                shots.add(shot);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(conn);
        }
        return shots;
    }

//    public List<Shot> getNext2ShotsForStrike(Shot shot) {
//        Connection conn = null;
//        PreparedStatement statement = null;
//        List<Shot> nextTwoShots = new ArrayList<>();
//        try {
//            conn = DatabaseUtil.connect();
//            String sql = "SELECT * FROM Shots WHERE game_shot_number BETWEEN ? AND ?";
//            statement = conn.prepareStatement(sql);
//            statement.setInt(1, shot.getGameShotNumber() + 1);
//            statement.setInt(2, shot.getGameShotNumber() + 2);
//            ResultSet resultSet = statement.executeQuery();
//            while(resultSet.next()) {
//                Shot nextShot = new Shot();
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    public Shot findByFrameNumberAndShotNumber(Frame frame, int shotNumber) {
        Connection conn = null;
        PreparedStatement statement = null;
        Shot shot = null;
        try {
            conn = DatabaseUtil.connect();
            String sql = "SELECT * FROM Shots WHERE frame_id = ? AND shot_number = ?";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, frame.getId());
            statement.setInt(2, shotNumber);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                shot = new Shot();
                shot.setId(resultSet.getInt("id"));
                shot.setFrame(frame);
                shot.setShotNumber(resultSet.getInt("shot_number"));
                shot.setShotValue(resultSet.getInt("shot_value"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(conn);
        }
        return shot;
    }

    private int getNextShotNumber(Frame frame) {
        Connection conn = null;
        PreparedStatement statement = null;
        int count = -1;
        try {
            conn = DatabaseUtil.connect();
            String sql = "SELECT COUNT(*) FROM Shots WHERE frame_id = ?";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, frame.getId());
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            count = resultSet.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(conn);
        }
        return count + 1;
    }

    public Shot createShot(Frame frame, int shotValue) {
        Shot shot = new Shot();
        shot.setFrame(frame);
        shot.setShotNumber(getNextShotNumber(frame));
        shot.setShotValue(shotValue);
        int id = insert(shot);
        shot.setId(id);
        return shot;
    }

    @Override
    public int insert(Shot row) {
        Connection conn = null;
        PreparedStatement statement = null;
        int generatedKey = 0;
        try {
            conn = DatabaseUtil.connect();
            statement = conn.prepareStatement("INSERT INTO Shots (frame_id, shot_number, shot_value) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, row.getFrame().getId());
            statement.setInt(2, row.getShotNumber());
            statement.setInt(3, row.getShotValue());
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
    public boolean update(Shot row) {
        return false;
    }
}
