package edu.uob;

public class BasicCommandTests {
    public BasicCommandTests() {}
    
    public static BasicCommandType fromString(String s) {
        switch (s.toLowerCase()) {
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
