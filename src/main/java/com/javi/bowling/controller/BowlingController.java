package com.javi.bowling.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.javi.bowling.dao.FrameDAO;
import com.javi.bowling.dao.GameDAO;
import com.javi.bowling.dao.PlayerDAO;
import com.javi.bowling.dao.ShotDAO;
import com.javi.bowling.enums.FrameType;
import com.javi.bowling.exception.GameAlreadyStartedException;
import com.javi.bowling.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class BowlingController {

    private Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private BowlingModel model = new BowlingModel();



    /**
     * The first resource a client should call in order to receive a game id. This handles the game generation.
     * @return the game id that will be used in subsequent requests
     */
    @RequestMapping(value = "/game", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity initGame() {
        GameDAO dao = new GameDAO();
        Game game = dao.initiateNewGame();
        String json = gson.toJson(game);
        return ResponseEntity.ok(json);
    }

    /**
     * Gets the entire scoreboard for a given game.
     * @return a json representation of the current scoreboard.
     */
    @RequestMapping(value = "/game", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getScoreBoard(@RequestParam(value="game_id") int gameId) {
        GameDAO gameDAO = new GameDAO();
        Game game = gameDAO.findById(gameId);
        List<Player> players = model.getPlayersInGame(game);
        JsonArray playerArray = new JsonArray();
        for(Player player : players) {
            JsonObject playerJson = gson.toJsonTree(player).getAsJsonObject();
            playerJson.addProperty("score", model.getScoreForPlayer(game, player));
            List<Frame> frames = model.getFramesForPlayer(game, player);
            JsonArray frameArray = new JsonArray();
            for(Frame frame : frames) {
                JsonObject frameObject = gson.toJsonTree(frame).getAsJsonObject();
                List<Shot> shots = model.getShotsInFrame(frame);
                JsonArray shotArray = gson.toJsonTree(shots).getAsJsonArray();
                frameObject.addProperty("score", model.getScoreForFrame(frame));
                frameObject.add("shots", shotArray);
                frameArray.add(frameObject);
            }
            playerJson.add("frames", frameArray);
            playerArray.add(playerJson);
        }
        return ResponseEntity.ok(gson.toJson(playerArray));
    }

    /**
     * Gets the frames for a player, which includes the frame types.
     * @param gameId the game we're retrieving the frames from
     * @param playerId the player we're retrieving the frames from.
     * @return a json representation of the frames belonging to the player.
     */
    @RequestMapping(value = "/frame", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getFramesForPlayer(@RequestParam(value="game_id") int gameId,
                                    @RequestParam(value="player_id") int playerId) {
        GameDAO gameDAO = new GameDAO();
        PlayerDAO playerDAO = new PlayerDAO();
        Game game = gameDAO.findById(gameId);
        Player player = playerDAO.findById(playerId);
        List<Frame> frames = model.getFramesForPlayer(game, player);
        String json = gson.toJson(frames);
        return ResponseEntity.ok(json);
    }

    /**
     * Gets the score for the given player.
     * @param playerId The player's id
     * @return JSON representation of the score for the player.
     */
    @RequestMapping(value = "/player_score", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getPlayerScore(@RequestParam(value="player_id") int playerId) {
        PlayerDAO playerDAO = new PlayerDAO();
        GameDAO gameDAO = new GameDAO();
        Player player = playerDAO.findById(playerId);
        Game game = gameDAO.findById(player.getId());
        int score = model.getScoreForPlayer(game, player);
        JsonObject scoreJson = new JsonObject();
        scoreJson.addProperty("player_score", score);
        String json = gson.toJson(scoreJson);
        return ResponseEntity.ok(json);
    }

    /**
     * Creates a player and associates it with a game.
     * @param name The name of the player
     * @param gameId The game we're adding the player to
     * @return Json representation of the player object that was created, with an ID that the client can use to manage the player. Will return HTTP 400 if the game has already begun.
     */
    @RequestMapping(value = "/player", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity createPlayer(@RequestParam(value="name", defaultValue = "player")String name,
                                       @RequestParam(value="game") int gameId) {
        PlayerDAO dao = new PlayerDAO();
        Player player = null;
        try {
            player = dao.createPlayer(name, gameId);
        } catch (GameAlreadyStartedException e) {
            e.printStackTrace();
            JsonObject object = new JsonObject();
            object.addProperty("error_message", e.getMessage());
            String json = gson.toJson(object);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(json);
        }
        String json = gson.toJson(player);
        return ResponseEntity.ok(json);
    }

    /**
     * Creates a Shot record and Frame record if needed.
     * @param gameId The game to put the shot under
     * @param playerId The player who made the shot.
     * @param pinsKnockedDown The number of pins knocked down in the shot.
     * @return HTTP 200 if the shot was updated successfully, with a json representation of the frame id that was modified/created. Or an HTTP 400 if the player already finished the game.
     */
    @RequestMapping(value = "/shot", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity takeShot(@RequestParam(value="game") int gameId,
                                   @RequestParam(value="player") int playerId,
                                   @RequestParam(value="pins_knocked_down") int pinsKnockedDown) {
        PlayerDAO playerDAO = new PlayerDAO();
        Player player = playerDAO.findById(playerId);

        GameDAO gameDAO = new GameDAO();
        Game game = gameDAO.findById(gameId);

        FrameDAO frameDAO = new FrameDAO();

        if(model.isGameFinished(game)) {
            JsonObject object = new JsonObject();
            object.addProperty("error_message", "All players have finished the game.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(gson.toJson(object));
        }

        if(model.isPlayerFinished(game, player)) {
            JsonObject object = new JsonObject();
            object.addProperty("error_message", "Player has already finished the game.");
            String json = gson.toJson(object);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(json);
        }

        Frame currentFrame = frameDAO.getCurrentFrame(game, player);
        if(currentFrame == null) {
            currentFrame = frameDAO.createFrame(game, player);
        }

        ShotDAO shotDAO = new ShotDAO();
        Shot shot = shotDAO.createShot(currentFrame, pinsKnockedDown);
        //There's slightly different rules when it comes to the tenth frame
        if(currentFrame.getFrameNumber() < 10) {
            handleStandardFrame(shotDAO, shot, frameDAO, currentFrame);
        }
        else {
            handleTenthFrame(shotDAO, shot, frameDAO, currentFrame);
        }
        String json = gson.toJson(currentFrame);
        return ResponseEntity.ok(json);
    }


    /* package-private */ void handleStandardFrame(ShotDAO shotDAO, Shot shot, FrameDAO frameDAO, Frame currentFrame) {
        if(shot.getShotValue() == 10) {
            currentFrame.setFrameType(FrameType.STRIKE);
        }
        else {
            Shot previousShot = shotDAO.getPreviousShot(shot);
            if(previousShot != null && previousShot.getShotValue() + shot.getShotValue() == 10) {
                currentFrame.setFrameType(FrameType.SPARE);
            }
            else if (previousShot != null && previousShot.getShotValue() + shot.getShotValue() < 10) {
                currentFrame.setFrameType(FrameType.OPEN);
            }
        }
        frameDAO.update(currentFrame);
    }

    /* package-private */ void handleTenthFrame(ShotDAO shotDAO, Shot shot, FrameDAO frameDAO, Frame currentFrame) {
        Shot previousShot = shotDAO.getPreviousShot(shot);
        if(previousShot != null && previousShot.getShotValue() + shot.getShotValue() < 10) {
            currentFrame.setFrameType(FrameType.OPEN);
            frameDAO.update(currentFrame);
        }
        else if(shot.getShotNumber() == 3) {
            Shot firstShot = shotDAO.findByFrameNumberAndShotNumber(currentFrame, 1);
            if(firstShot.getShotValue() == 10) {
                currentFrame.setFrameType(FrameType.STRIKE);
            }
            else if(previousShot.getShotValue() + firstShot.getShotValue() == 10) {
                currentFrame.setFrameType(FrameType.SPARE);
            }
            frameDAO.update(currentFrame);
        }
    }

}
