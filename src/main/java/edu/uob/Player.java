package edu.uob;

import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Math.min;

public class Player extends GameCharacter {
    private final ArrayList<Artefact> heldItems;
    private int health;
    
    public Player(String name) {
        super(name, "A player by the name of " + name);
        heldItems = new ArrayList<>();
        health = 3;
    }
    
    public void pickUpItem(Artefact a) {
        if (a != null) {
            heldItems.add(a);
        }
    }
    public void heal() {
        health = min(3, health + 1);
    }
    
    public void removeItem(Artefact a) throws IOException {
        if (heldItems.contains(a)) {
            heldItems.remove(a);
        } else {
            throw new IOException("ERROR: player cannot drop item that they " +
                "does not already hold");
        }
    }
    public void removeItem(String s) {
        heldItems.removeIf( a -> a.getName().equals(s));
    }
    
    public Artefact getItem(String s) {
        for (Artefact a : heldItems) {
            if (a.getName().equals(s)) {
                return a;
            }
        }
        return null;
    }
    
    public String dropItem(String[] words, Location l) throws IOException {
        for (String s : words) {
            for (Artefact a : heldItems) {
                if (a.getName().equals(s)) {
                    heldItems.remove(a);
                    l.addArtefact(a);
                    return (this.getName() + " dropped " + a.getName() + "\n");
                }
            }
        }
        return (this.getName() + " cannot drop an item they are not" +
            " holding\n");
    }
    public void takeDamage() {
        health -= 1;
    }
    
    public int getHealth() {
        return health;
    }
    public int getNumberOfHeldItems() {
        return heldItems.size();
    }
    
    public boolean itemHeld(Artefact a) {
        return heldItems.contains(a);
    }
    public boolean itemHeld(String s) {
        for (Artefact a : heldItems) {
            if (a.getName().equals(s)) {
                return true;
            }
        }
        return false;
    }
    
    public String listItems() {
        if (heldItems.size() == 0) {
            return "You are not currently holding any items\n";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("You are currently holding:\n");
        for (Artefact a : heldItems) {
            builder.append(a.getName())
                .append(" - ")
                .append(a.getDescription())
                .append(System.lineSeparator());
        }
        return builder.toString();
    }
    
    public boolean checkForDeath(Location current, Location start) throws IOException {
        if (health == 0) {
            ArrayList<Artefact> toDrop = new ArrayList<>();
            for (Artefact a : heldItems) {
                toDrop.add(a);
            }
            for (Artefact a : toDrop) {
                removeItem(a);
                current.addArtefact(a);
            }
            current.removeCharacter(this);
            start.addCharacter(this);
            health = 3;
            return true;
        } else {
            return false;
        }
    }
    
    public String reportHealth() {
        return (this.getName() + "'s health is at " + this.getHealth());
    }
}
