package com.javi.bowling.enums;

public enum FrameType {
    STRIKE,
    SPARE,
    OPEN;

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
