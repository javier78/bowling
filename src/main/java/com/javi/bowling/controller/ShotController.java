package com.javi.bowling.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.javi.bowling.dao.FrameDAO;
import com.javi.bowling.dao.GameDAO;
import com.javi.bowling.dao.PlayerDAO;
import com.javi.bowling.dao.ShotDAO;
import com.javi.bowling.enums.FrameType;
import com.javi.bowling.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShotController extends BaseController {
    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private BowlingModel model = new BowlingModel();

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
        if(pinsKnockedDown > 10) {
            JsonObject object = new JsonObject();
            object.addProperty("error_message", "More than 10 pins cannot be knocked down in a turn.");
        }
        PlayerDAO playerDAO = new PlayerDAO();
        Player player = playerDAO.findById(playerId);

        GameDAO gameDAO = new GameDAO();
        Game game = gameDAO.findById(gameId);

        if(player == null) {
            return generateErrorResponse("Invalid player id");
        } else if(game == null) {
            return generateErrorResponse("Invalid game id");
        }

        FrameDAO frameDAO = new FrameDAO();

        if(model.isGameFinished(game)) {
            return generateErrorResponse("All players have finished the game.");
        }

        if(model.isPlayerFinished(game, player)) {
            return generateErrorResponse("Player has already finished the game.");
        }

        Frame currentFrame = frameDAO.getCurrentFrame(game, player);
        if(currentFrame == null) {
            currentFrame = frameDAO.createFrame(game, player);
        }

        ShotDAO shotDAO = new ShotDAO();
        Shot shot = shotDAO.createShot(currentFrame, pinsKnockedDown);
        //There's slightly different rules when it comes to the tenth frame
        if(currentFrame.getFrameNumber() < 10) {
            try {
                handleStandardFrame(shotDAO, shot, frameDAO, currentFrame);
            } catch (IllegalArgumentException e) {
                return generateErrorResponse(e.getMessage());
            }
        }
        else {
            try {
                handleTenthFrame(shotDAO, shot, frameDAO, currentFrame);
            } catch(IllegalArgumentException e) {
                return generateErrorResponse(e.getMessage());
            }
        }
        String json = gson.toJson(currentFrame);
        return ResponseEntity.ok(json);
    }

    /**
     * Handles the situation of a shot being submitted for any frame that isn't the tenth.
     * @param shotDAO data access object from the shot method
     * @param shot shot object that was created based on the request
     * @param frameDAO data access object from the frame method
     * @param currentFrame frame object that the shot belongs to
     */
    private boolean handleStandardFrame(ShotDAO shotDAO, Shot shot, FrameDAO frameDAO, Frame currentFrame) throws IllegalArgumentException {
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
            else if(previousShot != null && previousShot.getShotValue() + shot.getShotValue() > 10) {
                throw new IllegalArgumentException("Shot points in a frame cannot exceed 10");
            }
        }
        frameDAO.update(currentFrame);
        return true;
    }

    /**
     * Handles the situation of a shot being submitted for the tenth frame.
     * @param shotDAO data access object from the shot method
     * @param shot shot object that was created based on the request
     * @param frameDAO data access object from the frame method
     * @param currentFrame frame object that the shot belongs to
     */
    private void handleTenthFrame(ShotDAO shotDAO, Shot shot, FrameDAO frameDAO, Frame currentFrame) throws IllegalArgumentException {
        Shot previousShot = shotDAO.getPreviousShot(shot);
        if(previousShot == null) {
            return;
        }
        //if this is the first shot in the 10th frame, nothing needs to change yet.
        //if this is the second shot, then we need to determine if the frame is an open frame. If not, we continue with the 3rd shot.
        if(shot.getShotNumber() == 2 && previousShot.getShotValue() + shot.getShotValue() < 10) {
            currentFrame.setFrameType(FrameType.OPEN);
            frameDAO.update(currentFrame);
        }
        //If this is the 3rd shot, we need to determine whether the frame is a strike or a spare.
        else if(shot.getShotNumber() == 3) {
            Shot firstShot = shotDAO.findByFrameNumberAndShotNumber(currentFrame, 1);
            //We can determine whether the frame was a strike if the first shot value is 10
            if(firstShot.getShotValue() + previousShot.getShotValue() + shot.getShotValue() > 30) {
                throw new IllegalArgumentException("Shot points in the 10th frame cannot exceed 30");
            }
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
