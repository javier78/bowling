package com.javi.bowling.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.javi.bowling.dao.GameDAO;
import com.javi.bowling.dao.PlayerDAO;
import com.javi.bowling.exception.GameAlreadyStartedException;
import com.javi.bowling.model.BowlingModel;
import com.javi.bowling.model.Game;
import com.javi.bowling.model.Player;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlayerController extends BaseController {
    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private BowlingModel model = new BowlingModel();
    /**
     * Gets the score for the given player.
     * @param playerId The player's id
     * @return JSON representation of the score for the player.
     */
    @RequestMapping(value = "/player_score", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity getPlayerScore(@RequestParam(value="game_id") int gameId,
                                         @RequestParam(value="player_id") int playerId) {
        PlayerDAO playerDAO = new PlayerDAO();
        GameDAO gameDAO = new GameDAO();
        Player player = playerDAO.findById(playerId);
        Game game = gameDAO.findById(gameId);
        if(game == null) {
            return generateErrorResponse("Invalid game id");
        } else if(player == null) {
            return generateErrorResponse("Invalid player id");
        }
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

        GameDAO gameDAO = new GameDAO();
        Game game = gameDAO.findById(gameId);
        if(game == null) {
            return generateErrorResponse("Invalid game id");
        }
        try {
            player = dao.createPlayer(name, game);
        } catch (GameAlreadyStartedException e) {
            e.printStackTrace();
            return generateErrorResponse(e.getMessage());
        }
        String json = gson.toJson(player);
        return ResponseEntity.ok(json);
    }
}
