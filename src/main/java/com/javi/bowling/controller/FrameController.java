package com.javi.bowling.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.javi.bowling.dao.GameDAO;
import com.javi.bowling.dao.PlayerDAO;
import com.javi.bowling.model.BowlingModel;
import com.javi.bowling.model.Frame;
import com.javi.bowling.model.Game;
import com.javi.bowling.model.Player;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FrameController extends BaseController {
    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private BowlingModel model = new BowlingModel();

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
        if(game == null) {
            return generateErrorResponse("Invalid game id");
        } else if(player == null) {
            return generateErrorResponse("Invalid player id");
        }
        List<Frame> frames = model.getFramesForPlayer(game, player);
        String json = gson.toJson(frames);
        return ResponseEntity.ok(json);
    }
}
