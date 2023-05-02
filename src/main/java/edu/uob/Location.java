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
    
    public void addFurniture(final Furniture f) {
        furniture.add(f);
    }
    public void addArtefact(final Artefact a) {
        artefacts.add(a);
    }
    public void addCharacter(final GameCharacter c) {
        characters.add(c);
    }
    public void addPath(final Location l) {
        paths.add(l);
    }
    
    public boolean furnitureIsPresent(final Furniture f) {
        return furniture.contains(f);
    }
    public boolean furnitureIsPresent(final String s) {
        for (final Furniture f : furniture) {
            if (f.getName().equals(s)) {
                return true;
            }
        }
        return false;
    }
    public boolean artefactIsPresent(final Artefact a) {
        return artefacts.contains(a);
    }
    public boolean artefactIsPresent(final String s) {
        for (final Artefact a : artefacts) {
            if (a.getName().equals(s)) {
                return true;
            }
        }
        return false;
    }
    public boolean characterIsPresent(final GameCharacter c) {
        return characters.contains(c);
    }
    public boolean characterIsPresent(final String s) {
        for (final GameCharacter c : characters) {
            if (c.getName().equals(s)) {
                return true;
            }
        }
        return false;
    }
    public boolean pathToLocationExists(final Location l) {
       return paths.contains(l);
    }
    public boolean pathToLocationExists(final String s) {
        for (final Location l : paths) {
            if (l.getName().equals(s)) {
                return true;
            }
        }
        return false;
    }
    
    public int furniturePresent() {
        return furniture.size();
    }
    public int artefactsPresent() {
        return artefacts.size();
    }
    public int charactersPresent() {
        return characters.size();
    }
    public int pathsPresent() {
        return paths.size();
    }
    
    public void removeFurniture(final Furniture f) {
        furniture.remove(f);
    }
    public Furniture removeFurniture(final String s) {
        for (final Furniture f : furniture) {
            if (f.getName().equals(s)) {
                furniture.remove(f);
                return f;
            }
        }
        return null;
    }
    public void removeArtefact(final Artefact a) {
        artefacts.remove(a);
    }
    public Artefact removeArtefact(final String s) {
        for (final Artefact a : artefacts) {
            if (a.getName().equals(s)) {
                artefacts.remove(a);
                return a;
            }
        }
        return null;
    }
    public void removeCharacter(final GameCharacter c) {
        characters.remove(c);
    }
    
    public GameCharacter removeCharacter(final String s) {
        for (final GameCharacter c : characters) {
            if (c.getName().equals(s)) {
                characters.remove(c);
                return c;
            }
        }
        return null;
    }
    
    public void removePath(final Location l) {
        paths.remove(l);
    }
    
    public ArrayList<GameCharacter> getCharacters() {
        return characters;
    }
    
    
    public String getArrivalString(final Player p) {
        final StringBuilder builder = new StringBuilder();
        builder.append("You arrive in ")
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
