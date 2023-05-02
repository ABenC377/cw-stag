package edu.uob;

import java.util.ArrayList;

public class Location extends GameEntity {
    private final ArrayList<Furniture> furniture;
    private final ArrayList<Artefact> artefacts;
    private final ArrayList<GameCharacter> characters;
    private final ArrayList<Location> paths;
    
    public Location(final String name, final String description) {
        super(name, description);
        furniture = new ArrayList<>();
        artefacts = new ArrayList<>();
        characters = new ArrayList<>();
        paths = new ArrayList<>();
    }
    
    public void addFurniture(final Furniture furniture) {
        this.furniture.add(furniture);
    }
    
    public void addArtefact(final Artefact artefact) {
        artefacts.add(artefact);
    }
    
    public void addCharacter(final GameCharacter character) {
        characters.add(character);
    }
    
    public void addPath(final Location location) {
        paths.add(location);
    }
    
    public boolean furnitureIsPresent(final String name) {
        for (final Furniture furn : furniture) {
            if (furn.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean artefactIsPresent(final Artefact artefact) {
        return artefacts.contains(artefact);
    }
    
    public boolean artefactIsPresent(final String name) {
        for (final Artefact artefact : artefacts) {
            if (artefact.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean characterIsPresent(final String name) {
        for (final GameCharacter character : characters) {
            if (character.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean pathToLocationExists(final String name) {
        for (final Location location : paths) {
            if (location.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    public Furniture removeFurniture(final String name) {
        for (final Furniture furn : furniture) {
            if (furn.getName().equals(name)) {
                furniture.remove(furn);
                return furn;
            }
        }
        return null;
    }
    
    public void removeArtefact(final Artefact artefact) {
        artefacts.remove(artefact);
    }
    
    public Artefact removeArtefact(final String name) {
        for (final Artefact artefact : artefacts) {
            if (artefact.getName().equals(name)) {
                artefacts.remove(artefact);
                return artefact;
            }
        }
        return null;
    }
    
    public void removeCharacter(final GameCharacter character) {
        characters.remove(character);
    }
    
    public GameCharacter removeCharacter(final String name) {
        for (final GameCharacter character : characters) {
            if (character.getName().equals(name)) {
                characters.remove(character);
                return character;
            }
        }
        return null;
    }
    
    public void removePath(final String name) {
        for (final Location destination : paths) {
            if (destination.getName().equals(name)) {
                paths.remove(destination);
            }
        }
    }
    
    public ArrayList<GameCharacter> getCharacters() {
        return characters;
    }
    
    
    public String getArrivalString(final Player player) {
        final StringBuilder builder = new StringBuilder();
        builder.append("You arrive in ")
            .append(this.getDescription())
            .append(" You can see:\n");
        for (final Artefact artefact : artefacts) {
            builder.append(artefact.getName())
                .append(": ")
                .append(artefact.getDescription())
                .append(System.lineSeparator());
        }
        for (final Furniture furn : furniture) {
            builder.append(furn.getName())
                .append(": ")
                .append(furn.getDescription())
                .append(System.lineSeparator());
        }
        for (final GameCharacter character : characters) {
            if (character != player) {
                builder.append(character.getName())
                    .append(": ")
                    .append(character.getDescription())
                    .append(System.lineSeparator());
            }
        }
        builder.append("You can see from here:\n");
        for (final Location location : paths) {
            builder.append(location.getName());
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }
    
    public String lookAround(final Player p) {
        final StringBuilder builder = new StringBuilder();
        builder.append("You are in ")
            .append(this.getDescription())
            .append(" You can see:\n");
        for (final Artefact a : artefacts) {
            builder.append(a.getName())
                .append(": ")
                .append(a.getDescription())
                .append(System.lineSeparator());
        }
        for (final Furniture f : furniture) {
            builder.append(f.getName())
                .append(": ")
                .append(f.getDescription())
                .append(System.lineSeparator());
        }
        for (final GameCharacter c : characters) {
            if (c != p) {
                builder.append(c.getName())
                    .append(": ")
                    .append(c.getDescription())
                    .append(System.lineSeparator());
            }
        }
        builder.append("You can see from here:\n");
        for (final Location l : paths) {
            builder.append(l.getName());
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }
    
    public void produce(final String entityName, final ArrayList<Location> locations) {
        for (final Location l : locations) {
            if (l.getName().equals(entityName)) {
                paths.add(l);
            }
            if (l.characterIsPresent(entityName)) {
                characters.add(l.removeCharacter(entityName));
                return;
            } else if (l.furnitureIsPresent(entityName)) {
                furniture.add(l.removeFurniture(entityName));
                return;
            } else if (l.artefactIsPresent(entityName)) {
                artefacts.add(l.removeArtefact(entityName));
                return;
            }
        }
    }
    
}
