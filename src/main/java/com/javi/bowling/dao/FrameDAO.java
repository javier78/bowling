package com.javi.bowling.dao;

import com.javi.bowling.enums.FrameType;
import com.javi.bowling.model.DatabaseUtil;
import com.javi.bowling.model.Frame;
import com.javi.bowling.model.Game;
import com.javi.bowling.model.Player;
import org.apache.commons.dbutils.DbUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FrameDAO implements IDAO<Frame> {
    @Override
    public List<Frame> findAll() {
        List<Frame> frames = new ArrayList<>();
        Connection conn = null;
        Statement statement = null;
        try {
            conn = DatabaseUtil.connect();
            String sql = "SELECT * FROM Frames;";
            statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
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
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(conn);
        }
        return frames;
    }

    @Override
    public Frame findById(int id) {
        Frame frame = new Frame();
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = DatabaseUtil.connect();
            String sql = "SELECT * FROM Frames WHERE id = ?";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
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
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(conn);
        }
        return frame;
    }

    /**
     * Gets the Frame record that has a frame_type of CURRENT.
     * @param game the game_id to search on
     * @param player the player_id to search on.
     * @return the Frame record that has a frame_type of CURRENT, null if no such frame exists
     */
    public Frame getCurrentFrame(Game game, Player player) {
       Connection conn = null;
       PreparedStatement statement = null;
       Frame frame = null;

       try {
           conn = DatabaseUtil.connect();
           String sql = "SELECT * FROM Frames WHERE game_id = ? AND player_id = ? AND frame_type = ?";
           statement = conn.prepareStatement(sql);
           statement.setInt(1, game.getId());
           statement.setInt(2, player.getId());
           statement.setString(3, FrameType.CURRENT.toString());
           ResultSet resultSet = statement.executeQuery();
           if(resultSet.next()) {
               frame = new Frame();
               frame.setId(resultSet.getInt("id"));
               frame.setPlayer(player);
               frame.setGame(game);
               frame.setFrameNumber(resultSet.getInt("frame_number"));
               frame.setFrameType(FrameType.getTypeByName(resultSet.getString("frame_type")));
           }
       } catch (SQLException e) {
           e.printStackTrace();
       } finally {
           DbUtils.closeQuietly(statement);
           DbUtils.closeQuietly(conn);
       }
       return frame;
    }

    /**
     * When creating a new frame for a given player and game, this needs to be called in order to determine the next frame in the sequence.
     * @param game The game_id belonging to the frame group
     * @param player the player_id belonging to the frame group
     * @return the next frame number in the sequence.
     */
    private int getNextFrameNumber(Game game, Player player) {
        Connection conn = null;
        PreparedStatement statement = null;
        int count = -1;
        try {
            conn = DatabaseUtil.connect();
            String sql = "SELECT COUNT(*) FROM Frames WHERE game_id = ? AND player_id = ?";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, game.getId());
            statement.setInt(2, player.getId());

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

    /**
     * Helper method for creating a frame
     * @param game the game the new frame belongs to
     * @param player the player the new frame belongs to
     * @return the frame object with its id field populated to reflect the value in the database.
     */
    public Frame createFrame(Game game, Player player) {
        Frame frame = new Frame();
        frame.setGame(game);
        frame.setPlayer(player);
        frame.setFrameNumber(getNextFrameNumber(game, player));
        frame.setFrameType(FrameType.CURRENT);
        int id = insert(frame);
        frame.setId(id);
        return frame;
    }

    @Override
    public int insert(Frame row) {
        Connection conn = null;
        PreparedStatement statement = null;
        int generatedKey = 0;
        try {
            conn = DatabaseUtil.connect();
            statement = conn.prepareStatement("INSERT INTO Frames (game_id, player_id, frame_number, frame_type) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, row.getGame().getId());
            statement.setInt(2, row.getPlayer().getId());
            statement.setInt(3, row.getFrameNumber());
            statement.setString(4, row.getFrameType().toString());
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
    public boolean update(Frame row) {
        Connection conn = null;
        PreparedStatement statement = null;
        boolean successful = false;
        try {
            conn = DatabaseUtil.connect();
            statement = conn.prepareStatement("UPDATE Frames SET frame_number = ?, frame_type = ? WHERE id = ?");
            statement.setInt(1, row.getFrameNumber());
            statement.setString(2, row.getFrameType().toString());
            statement.setInt(3, row.getId());
            successful = statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(conn);
        }
        return successful;
    }
}
