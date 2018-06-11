package com.javi.bowling.model;

import com.google.gson.annotations.Expose;
import com.javi.bowling.enums.FrameType;

public class Frame {
    @Expose
    private int id;
    @Expose(serialize = false)
    private Game game;
    @Expose(serialize = false)
    private Player player;
    @Expose
    private int frameNumber;
    @Expose
    private FrameType frameType;

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public void setFrameNumber(int frameNumber) {
        this.frameNumber = frameNumber;
    }

    public FrameType getFrameType() {
        return frameType;
    }

    public void setFrameType(FrameType frameType) {
        this.frameType = frameType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
