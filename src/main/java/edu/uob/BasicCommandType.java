package edu.uob;

import java.util.Locale;

/**
 * enum for the different types of built-in command
 */
public enum BasicCommandType {
    INV ("inv"),
    GET ("get"),
    DROP ("drop"),
    GOTO ("goto"),
    LOOK ("look"),
    HEALTH ("health"),
    NULL ("null"),
    ERROR ("error");
    
    private final String name;
    
    /**
     * construtore for enum to give them a string name
     * @param name the string corresponding to the enum
     */
    BasicCommandType(final String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
    /**
     * Static method for getting the enum type from its string(s)
     * @param string the user input word
     * @return the corresponding enum type, (NULL or ERROR also allowed)
     */
    public static BasicCommandType fromString(final String string) {
        BasicCommandType output;
        if ("inventory".equals(string.toLowerCase(Locale.ENGLISH)) ||
            "inv".equals(string.toLowerCase(Locale.ENGLISH))) {
            output = BasicCommandType.INV;
        } else if ("get".equals(string.toLowerCase(Locale.ENGLISH))) {
            output = BasicCommandType.GET;
        } else if ("drop".equals(string.toLowerCase(Locale.ENGLISH))) {
            output = BasicCommandType.DROP;
        } else if ("goto".equals(string.toLowerCase(Locale.ENGLISH))) {
            output = BasicCommandType.GOTO;
        } else if ("look".equals(string.toLowerCase(Locale.ENGLISH))) {
            output = BasicCommandType.LOOK;
        } else if ("health".equals(string.toLowerCase(Locale.ENGLISH))) {
            output = BasicCommandType.HEALTH;
        } else {
            output = BasicCommandType.NULL;
        }
        return output;
    }
}
