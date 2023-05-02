package edu.uob;

import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Math.min;

public class Player extends GameCharacter {
    private final ArrayList<Artefact> heldItems;
    private int health;
    
    public Player(final String name) {
        super(name, "A player by the name of " + name);
        heldItems = new ArrayList<>();
        health = 3;
    }
    
    public void pickUpItem(final Artefact artefact) {
        if (artefact != null) {
            heldItems.add(artefact);
        }
    }
    public void heal() {
        health = min(3, health + 1);
    }
    
    public void removeItem(final Artefact artefact) throws IOException {
        if (heldItems.contains(artefact)) {
            heldItems.remove(artefact);
        } else {
            throw new IOException("ERROR: player cannot drop item that they " +
                "does not already hold");
        }
    }
    public void removeItem(final String name) {
        heldItems.removeIf(artefact -> artefact.getName().equals(name));
    }
    
    public Artefact getItem(final String name) {
        for (final Artefact artefact : heldItems) {
            if (artefact.getName().equals(name)) {
                return artefact;
            }
        }
        return null;
    }
    
    public String dropItem(final String[] words,
                           final Location location) throws IOException {
        for (final String word : words) {
            for (final Artefact artefact : heldItems) {
                if (artefact.getName().equals(word)) {
                    heldItems.remove(artefact);
                    location.addArtefact(artefact);
                    return (this.getName() + " dropped " +
                        artefact.getName() + "\n");
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
    
    public boolean itemHeld(final Artefact artefact) {
        return heldItems.contains(artefact);
    }
    
    public boolean itemHeld(final String name) {
        for (final Artefact artefact : heldItems) {
            if (artefact.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    public String listItems() {
        if (heldItems.isEmpty()) {
            return "You are not currently holding any items\n";
        }
        final StringBuilder builder = new StringBuilder();
        builder.append("You are currently holding:\n");
        for (final Artefact a : heldItems) {
            builder.append(a.getName())
                .append(" - ")
                .append(a.getDescription())
                .append(System.lineSeparator());
        }
        return builder.toString();
    }
    
    public boolean checkForDeath(final Location current,
                                 final Location start) throws IOException {
        if (health == 0) {
            final ArrayList<Artefact> toDrop = new ArrayList<>();
            for (final Artefact a : heldItems) {
                toDrop.add(a);
            }
            for (final Artefact a : toDrop) {
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
