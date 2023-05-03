package edu.uob;

import java.util.Locale;

/**
 * enum for the different types of built-in command
 */
public enum BasicCommandType {
    INV, GET, DROP, GOTO, LOOK, HEALTH, NULL, ERROR;
    
    /**
     * Static method for getting the enum type from its string(s)
     * @param string the user input word
     * @return the corresponding enum type, (NULL or ERROR also allowed)
     */
    public static BasicCommandType fromString(final String string) {
        BasicCommandType output;
        switch (string.toLowerCase(Locale.ENGLISH)) {
            case "inventory", "inv" -> output = BasicCommandType.INV;
            case "get" -> output = BasicCommandType.GET;
            case "drop" -> output = BasicCommandType.DROP;
            case "goto" -> output = BasicCommandType.GOTO;
            case "look" -> output = BasicCommandType.LOOK;
            case "health" -> output = BasicCommandType.HEALTH;
            default -> output = BasicCommandType.NULL;
        }
        return output;
    }
}
