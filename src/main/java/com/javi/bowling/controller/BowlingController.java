package com.javi.bowling.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BowlingController {
    @RequestMapping(value = "/start_game", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity startGame() {

    }
}
