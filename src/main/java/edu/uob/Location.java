package edu.uob;

import java.util.ArrayList;

public class Location extends GameEntity {
    private final ArrayList<Furniture> furniture;
    private final ArrayList<Artefact> artefacts;
    private final ArrayList<GameCharacter> characters;
    private final ArrayList<Location> paths;
    
    public Location(String name, String description) {
        super(name, description);
        furniture = new ArrayList<>();
        artefacts = new ArrayList<>();
        characters = new ArrayList<>();
        paths = new ArrayList<>();
    }
    
    public void addFurniture(Furniture f) {
        furniture.add(f);
    }
    public void addArtefact(Artefact a) {
        artefacts.add(a);
    }
    public void addCharacter(GameCharacter c) {
        characters.add(c);
    }
    public void addPath(Location l) {
        paths.add(l);
    }
    
    public boolean furnitureIsPresent(Furniture f) {
        return furniture.contains(f);
    }
    public boolean furnitureIsPresent(String s) {
        for (final Furniture f : furniture) {
            if (f.getName().equals(s)) {
                return true;
            }
        }
        return false;
    }
    public boolean artefactIsPresent(Artefact a) {
        return artefacts.contains(a);
    }
    public boolean artefactIsPresent(String s) {
        for (final Artefact a : artefacts) {
            if (a.getName().equals(s)) {
                return true;
            }
        }
        return false;
    }
    public boolean characterIsPresent(GameCharacter c) {
        return characters.contains(c);
    }
    public boolean characterIsPresent(String s) {
        for (final GameCharacter c : characters) {
            if (c.getName().equals(s)) {
                return true;
            }
        }
        return false;
    }
    public boolean pathToLocationExists(Location l) {
       return paths.contains(l);
    }
    public boolean pathToLocationExists(String s) {
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
    
    public void removeFurniture(Furniture f) {
        furniture.remove(f);
    }
    public Furniture removeFurniture(String s) {
        for (final Furniture f : furniture) {
            if (f.getName().equals(s)) {
                furniture.remove(f);
                return f;
            }
        }
        return null;
    }
    public void removeArtefact(Artefact a) {
        artefacts.remove(a);
    }
    public Artefact removeArtefact(String s) {
        for (final Artefact a : artefacts) {
            if (a.getName().equals(s)) {
                artefacts.remove(a);
                return a;
            }
        }
        return null;
    }
    public void removeCharacter(GameCharacter c) {
        characters.remove(c);
    }
    
    public GameCharacter removeCharacter(String s) {
        for (final GameCharacter c : characters) {
            if (c.getName().equals(s)) {
                characters.remove(c);
                return c;
            }
        }
        return null;
    }
    
    public void removePath(Location l) {
        paths.remove(l);
    }
    
    public ArrayList<GameCharacter> getCharacters() {
        return characters;
    }
    
    
    public String getArrivalString(Player p) {
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
    
    public String lookAround(Player p) {
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
    
    public void produce(String entityName, ArrayList<Location> locations) {
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
