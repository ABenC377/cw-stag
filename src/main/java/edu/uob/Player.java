package edu.uob;

import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Math.min;

/**
 * an object for a player controlled by a user - subclass of the character class
 */
public class Player extends GameCharacter {
    /**
     * a list of the artefacts held by the player
     */
    private final ArrayList<Artefact> heldItems;
    /**
     * the player's health (0-3)
     */
    private int health;
    
    /**
     * produces a player object, with an empty inventory, and 3 health
     * @param name the name of the player
     */
    public Player(final String name) {
        super(name, "A player by the name of " + name);
        heldItems = new ArrayList<>();
        health = 3;
    }
    
    /**
     * adds an artefact object to player's inventory
     * @param artefact the artefact object
     */
    public void pickUpItem(final Artefact artefact) {
        if (artefact != null) {
            heldItems.add(artefact);
        }
    }
    
    /**
     * heals the player by one point, to a maximum of three
     */
    public void heal() {
        health = min(3, health + 1);
    }
    
    /**
     * removes an artefact from the player's inventory
     * @param artefact the artefact opbject being removed
     * @throws IOException self-evident
     */
    public void removeItem(final Artefact artefact) throws IOException {
        if (heldItems.contains(artefact)) {
            heldItems.remove(artefact);
        } else {
            throw new IOException("ERROR: player cannot drop item that they " +
                "does not already hold");
        }
    }
    
    /**
     * removes an artefact from the player's inventory
     * @param name the name of teh artefact being removed
     */
    public void removeItem(final String name) {
        heldItems.removeIf(artefact -> artefact.getName().equals(name));
    }
    
    /**
     * gets a pointer to a held artefact object
     * @param name the name of the object being got
     * @return the artefact object, or null if no such artefact is held
     */
    public Artefact getItem(final String name) {
        for (final Artefact artefact : heldItems) {
            if (artefact.getName().equals(name)) {
                return artefact;
            }
        }
        return null;
    }
    
    /**
     * decreases the player's health by one point
     */
    public void takeDamage() {
        health -= 1;
    }
    
    /**
     * @return the player's current health, as an int
     */
    public int getHealth() {
        return health;
    }
    
    /**
     * checks whether an item is held
     * @param artefact the  artefact object that is being checked
     * @return yes/no
     */
    public boolean itemHeld(final Artefact artefact) {
        return heldItems.contains(artefact);
    }
    
    /**
     * checks whether an item is held
     * @param name the name of teh item that is being checked
     * @return yes/no
     */
    public boolean itemHeld(final String name) {
        for (final Artefact artefact : heldItems) {
            if (artefact.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * gives a summary of the items being held by this player
     * @return a string that is to be sent to the client
     */
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
    
    /**
     * checks whether the player has died, and reset's them if they have
     * @param current the location object for where the player currently is
     * @param start the location object for the start of the game
     * @return yes/no
     * @throws IOException self-evident
     */
    public boolean checkForDeath(final Location current,
                                 final Location start) throws IOException {
        if (health == 0) {
            final ArrayList<Artefact> toDrop = new ArrayList<>(heldItems);
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
        return this.getName() + "'s health is at " + this.getHealth();
    }
}
