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
        for (Furniture f : furniture) {
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
        for (Artefact a : artefacts) {
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
        for (GameCharacter c : characters) {
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
        for (Location l : paths) {
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
        for (Furniture f : furniture) {
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
        for (Artefact a : artefacts) {
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
        for (GameCharacter c : characters) {
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
        StringBuilder builder = new StringBuilder();
        builder.append("You arrive in ");
        builder.append(this.getDescription());
        builder.append(" You can see:\n");
        for (Artefact a : artefacts) {
            builder.append(a.getDescription());
            builder.append("\n");
        }
        for (Furniture f : furniture) {
            builder.append(f.getDescription());
            builder.append("\n");
        }
        for (GameCharacter c : characters) {
            if (c != p) {
                builder.append(c.getDescription());
                builder.append("\n");
            }
        }
        builder.append("You can see from here:\n");
        for (Location l : paths) {
            builder.append(l.getDescription());
            builder.append("\n");
        }
        return builder.toString();
    }
    
    public String lookAround(Player p) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are in ");
        builder.append(this.getDescription());
        builder.append(" You can see:\n");
        for (Artefact a : artefacts) {
            builder.append(a.getDescription());
            builder.append("\n");
        }
        for (Furniture f : furniture) {
            builder.append(f.getDescription());
            builder.append("\n");
        }
        for (GameCharacter c : characters) {
            if (c != p) {
                builder.append(c.getDescription());
                builder.append("\n");
            }
        }
        builder.append("You can see from here:\n");
        for (Location l : paths) {
            builder.append(l.getDescription());
            builder.append("\n");
        }
        return builder.toString();
    }
    
    public void produce(String entityName, ArrayList<Location> locations) {
        for (Location l : locations) {
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
