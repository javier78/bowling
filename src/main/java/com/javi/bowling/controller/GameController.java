package com.javi.bowling.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.javi.bowling.dao.GameDAO;
import com.javi.bowling.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GameController extends BaseController {

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
        if(game == null) {
            return generateErrorResponse("Game id is invalid");
        }
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
}
