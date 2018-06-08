package com.javi.bowling.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.javi.bowling.dao.FrameDAO;
import com.javi.bowling.dao.GameDAO;
import com.javi.bowling.dao.PlayerDAO;
import com.javi.bowling.dao.ShotDAO;
import com.javi.bowling.enums.FrameType;
import com.javi.bowling.exception.GameAlreadyStartedException;
import com.javi.bowling.model.Frame;
import com.javi.bowling.model.Game;
import com.javi.bowling.model.Player;
import com.javi.bowling.model.Shot;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BowlingController {
    @RequestMapping(value = "/game", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity initGame() {
        GameDAO dao = new GameDAO();
        Game game = dao.initiateNewGame();
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(game);
        return ResponseEntity.ok(json);
    }

    @RequestMapping(value = "/player", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity createPlayer(@RequestParam(value="name", defaultValue = "player")String name,
                                       @RequestParam(value="game") int gameId) {
        Gson gson = new GsonBuilder().create();
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

    @RequestMapping(value = "/shot", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity takeShot(@RequestParam(value="game") int gameId,
                                   @RequestParam(value="player") int playerId,
                                   @RequestParam(value="pins_knocked_down") int pinsKnockedDown) {
        Gson gson = new GsonBuilder().create();
        PlayerDAO playerDAO = new PlayerDAO();
        Player player = playerDAO.findById(playerId);

        GameDAO gameDAO = new GameDAO();
        Game game = gameDAO.findById(gameId);

        FrameDAO frameDAO = new FrameDAO();

        if(frameDAO.isPlayerFinished(game, player)) {
            JsonObject object = new JsonObject();
            object.addProperty("error_message", "Player has already finished the game.");
            String json = gson.toJson(object);
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(json);
        }

        Frame currentFrame = frameDAO.getCurrentFrame(game, player);
        if(currentFrame == null) {
            currentFrame = frameDAO.createFrame(game, player);
        }

        ShotDAO shotDAO = new ShotDAO();
        Shot shot = shotDAO.createShot(currentFrame, pinsKnockedDown);
        //There's slightly different rules when it comes to the tenth frame
        if(currentFrame.getFrameNumber() < 10) {
            if(shot.getShotValue() == 10) {
                currentFrame.setFrameType(FrameType.STRIKE);
                frameDAO.update(currentFrame);
            }
            else {
                Shot previousShot = shotDAO.getPreviousShot(shot);
                if(previousShot != null && previousShot.getShotValue() + shot.getShotValue() == 10) {
                    currentFrame.setFrameType(FrameType.SPARE);
                }
                else if (previousShot != null && previousShot.getShotValue() + shot.getShotValue() < 10) {
                    currentFrame.setFrameType(FrameType.OPEN);
                }
                frameDAO.update(currentFrame);
            }
        }
        else {
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

        return ResponseEntity.ok().build();
    }
}
