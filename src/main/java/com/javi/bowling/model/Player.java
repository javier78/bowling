package com.javi.bowling.model;

import com.google.gson.annotations.Expose;

public class Player {
    @Expose
    private int id;
    @Expose
    private String name;
    @Expose(serialize = false)
    private Game game;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Game getGame() {
        return game;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
