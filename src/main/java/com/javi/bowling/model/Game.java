package com.javi.bowling.model;

import com.google.gson.annotations.Expose;

public class Game {
    @Expose
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
