package com.javi.bowling.enums;

public enum FrameType {
    CURRENT(1),
    STRIKE(3),
    SPARE(3),
    OPEN(2);

    int shotsToAdd;

    FrameType(int shotsToAdd) {
        this.shotsToAdd = shotsToAdd;
    }

    public int getShotsToAdd() {
        return shotsToAdd;
    }

    /**
     * Translates a String to a FrameType enum
     * @param name The FrameType as it appears in the database
     * @return The FrameType with a matching name, null if name doesn't match an enum.
     */
    public static FrameType getTypeByName(String name) {
        for(FrameType type : FrameType.values()) {
            if(type.toString().equals(name)) {
                return type;
            }
        }
        return null;
    }
}
