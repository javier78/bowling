package com.javi.bowling.model;

import com.javi.bowling.dao.ShotDAO;
import com.javi.bowling.enums.FrameType;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is meant for miscellaneous database queries that don't belong to a specific DAO.
 */
public class BowlingModel {

    /**
     * Determines whether the given player in the game is able to throw more shots in the game.
     * @param game The game the player is in
     * @param player The player to check
     * @return true if the player completed their 10th frame, false if not.
     */
    public boolean isPlayerFinished(Game game, Player player) {
        Connection conn = null;
        PreparedStatement statement = null;
        boolean finished = false;
        try {
            String sql = "SELECT COUNT(*) FROM Frames WHERE game_id = ? AND player_id = ? AND frame_number = 10 AND frame_type != 'CURRENT'";
            conn = DatabaseUtil.connect();
            statement = conn.prepareStatement(sql);
            statement.setInt(1, game.getId());
            statement.setInt(2, player.getId());
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(conn);
        }
        return finished;
    }

    /**
     * Gets the player objects belonging to a game
     * @param game The game we're retrieving player objects from.
     * @return a list of player objects in the game.
     */
    public List<Player> getPlayersInGame(Game game) {
        Connection conn = null;
        PreparedStatement statement = null;
        ArrayList<Player> players = new ArrayList<>();
        try {
            conn = DatabaseUtil.connect();
            String sql = "SELECT * FROM Players WHERE Players.game_id = ?";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, game.getId());
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Player player = new Player();
                player.setId(resultSet.getInt("id"));
                player.setName(resultSet.getString("name"));
                player.setGame(game);
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

    /**
     * Gets the shot records belonging to a frame.
     * @param frame the frame we're getting the shots from
     * @return a list of shots.
     */
    public List<Shot> getShotsInFrame(Frame frame) {
        Connection conn = null;
        PreparedStatement statement = null;
        ArrayList<Shot> shots = new ArrayList<>();
        try {
            String sql = "SELECT * FROM Shots WHERE frame_id = ?";
            conn = DatabaseUtil.connect();
            statement = conn.prepareStatement(sql);
            statement.setInt(1, frame.getId());
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Shot shot = new Shot();
                shot.setId(resultSet.getInt("id"));
                shot.setFrame(frame);
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

    /**
     * Gets the frames belonging to a player.
     * @param game The game the player belongs to.
     * @param player The player we're getting the frames for.
     * @return A list of frames belonging to a player.
     */
    public List<Frame> getFramesForPlayer(Game game, Player player) {
        Connection conn = null;
        PreparedStatement statement = null;
        ArrayList<Frame> frames = new ArrayList<>();
        try {
            conn = DatabaseUtil.connect();
            String sql = "SELECT * FROM Frames WHERE game_id = ? AND player_id = ? ORDER BY frame_number";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, game.getId());
            statement.setInt(2, player.getId());
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()) {
                Frame frame = new Frame();
                frame.setId(resultSet.getInt("id"));
                frame.setGame(game);
                frame.setPlayer(player);
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



    /**
     * Returns the current score for a player.
     * @param player
     * @return An up-to-date score for a player
     */
    public int getScoreForPlayer(Game game, Player player) {
        List<Frame> frames = getFramesForPlayer(game, player);
        int score = 0;
        for(Frame frame : frames) {
            score += getScoreForFrame(frame);
        }
        return score;
    }

    /**
     * Returns the score for a given frame.
     * @param frame the frame we're getting the score from
     * @return The frame's score.
     */
    public int getScoreForFrame(Frame frame) {
        ShotDAO shotDAO = new ShotDAO();
        List<Shot> shots;
        int score = 0;
        if(frame.getFrameType() == FrameType.STRIKE) {
            shots = shotDAO.getNextNShots(frame, FrameType.STRIKE.getShotsToAdd());
        } else if(frame.getFrameType() == FrameType.SPARE) {
            shots = shotDAO.getNextNShots(frame, FrameType.SPARE.getShotsToAdd());
        } else {
            shots = getShotsInFrame(frame);
        }

        for(Shot shot : shots) {
            score += shot.getShotValue();
        }
        return score;
    }

    /**
     * Determines whether the game is finished
     * @param game The game to check
     * @return true if all the players completed their 10th frame, false otherwise.
     */
    public boolean isGameFinished(Game game) {
        List<Player> players = getPlayersInGame(game);
        for(Player player : players) {
            if(!isPlayerFinished(game, player)) {
                return false;
            }
        }
        return true;
    }
//    public ArrayList<Frame> getFramesInGame() {
//        Connection conn = null;
//        PreparedStatement statement = null;
//        ArrayList<Frame> frames = new ArrayList<>();
//        try {
//            String sql = "SELECT ";
//        }
//    }
}
