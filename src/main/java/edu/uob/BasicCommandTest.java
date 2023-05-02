package edu.uob;

public class BasicCommandTest {
    public BasicCommandTest() {}
    
    public static BasicCommand fromString(String s) {
        switch (s.toLowerCase()) {
            case "inventory", "inv" -> {
                return BasicCommand.INV;
            }
            case "get" -> {
                return BasicCommand.GET;
            }
            case "drop" -> {
                return BasicCommand.DROP;
            }
            case "goto" -> {
                return BasicCommand.GOTO;
            }
            case "look" -> {
                return BasicCommand.LOOK;
            }
            case "health" -> {
                return BasicCommand.HEALTH;
            }
            default -> {
                return BasicCommand.NULL;
            }
        }
    }
}
