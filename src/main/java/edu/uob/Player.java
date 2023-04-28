package edu.uob;

import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Math.min;

public class Player extends GameCharacter {
    private ArrayList<Artefact> heldItems;
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
    public void removeItem(String s) throws IOException {
        for (Artefact a : heldItems) {
            if (a.getName().equals(s)) {
                heldItems.remove(a);
            }
        }
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
            builder.append(a.getName());
            builder.append(" - ");
            builder.append(a.getDescription());
            builder.append("\n");
        }
        return builder.toString();
    }
    
    public String getArtefactFromLocation(String[] words,
                                           Location l) {
        for (String w : words) {
            Artefact a = l.removeArtefact(w);
            if (a != null) {
                this.pickUpItem(a);
                return (this.getName() + " picked up " + a.getName() + "\n");
            }
        }
        return ("There is no such item in the \n" + l.getName());
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
}
