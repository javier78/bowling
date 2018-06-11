package com.javi.bowling.model;

import com.google.gson.annotations.Expose;

public class Shot {
    @Expose
    private int id;
    @Expose(serialize = false)
    private Frame frame;
    @Expose
    private int shotNumber;
    @Expose
    private int shotValue;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Frame getFrame() {
        return frame;
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
    }

    public int getShotNumber() {
        return shotNumber;
    }

    public void setShotNumber(int shotNumber) {
        this.shotNumber = shotNumber;
    }

    public int getShotValue() {
        return shotValue;
    }

    public void setShotValue(int shotValue) {
        this.shotValue = shotValue;
    }
}
