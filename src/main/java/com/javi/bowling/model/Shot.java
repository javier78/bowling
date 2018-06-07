package com.javi.bowling.model;

public class Shot {
    private int id;
    private Frame frame;
    private int shotNumber;
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
