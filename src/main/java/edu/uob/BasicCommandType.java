package edu.uob;

import java.util.Locale;

public enum BasicCommandType {
    INV, GET, DROP, GOTO, LOOK, HEALTH, NULL, ERROR;
    
    public static BasicCommandType fromString(final String string) {
        switch (string.toLowerCase(Locale.ENGLISH)) {
            case "inventory", "inv" -> {
                return BasicCommandType.INV;
            }
            case "get" -> {
                return BasicCommandType.GET;
            }
            case "drop" -> {
                return BasicCommandType.DROP;
            }
            case "goto" -> {
                return BasicCommandType.GOTO;
            }
            case "look" -> {
                return BasicCommandType.LOOK;
            }
            case "health" -> {
                return BasicCommandType.HEALTH;
            }
            default -> {
                return BasicCommandType.NULL;
            }
        }
    }
}
