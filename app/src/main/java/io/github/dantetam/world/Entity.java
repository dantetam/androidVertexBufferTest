package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.List;

import static io.github.dantetam.world.Action.ActionType;
import static io.github.dantetam.world.Action.ActionStatus;

/**
 * Created by Dante on 6/13/2016.
 */
public abstract class Entity extends Representable {

    protected Tile location;
    public String name;
    public Clan clan;
    public List<Item> items;
    public
    public static int globalIdCounter = 0;
    public int id;
    public World world;

    public List<Action> actionsQueue; //Do actions at position 0 first
    public int actionPoints, maxActionPoints;

    public Entity(World w, Clan c) {
        //locations = new ArrayList<Tile>();
        world = w;
        clan = c;
        items = new ArrayList<Item>();
        id = globalIdCounter;
        Entity.globalIdCounter++;
        actionsQueue = new ArrayList<>();
    }

    public void move(Tile t) {
        if (location != null) {
            location.occupants.remove(this);
        }
        location = t;
        t.occupants.add(this);
        /*int deltaX = t.row - locations.get(0).row;
        int deltaY = t.col - locations.get(0).col;
        Tile center = locations.remove(0);
        for (int i = 0; i < locations.size(); i++) {
            Tile old = locations.get(i);
            Tile newLocation = world.getTile(old.row + deltaX, old.col + deltaY);
            locations.set(i, newLocation);
        }
        locations.add(0, world.getTile(center.row + deltaX, center.col + deltaY));*/
    }

    public Tile location() {
        return location;
    }

    public boolean equals(Object other) {
        if (!(other instanceof Entity)) {
            return false;
        }
        Entity en = (Entity) other;
        //return location.equals(en.location) && name.equals(en.name) &&
        return id == en.id;
    }

    public ActionStatus gameMove(Tile t) {
        Tile location = location();
        if (location != null) {
            float dist = location.dist(t);
            if (actionPoints <= 0) {
                return ActionStatus.OUT_OF_ENERGY;
            }
            if (dist == 1) {
                //actionPoints--;
                move(t);
                return ActionStatus.EXECUTED;
            }
            else if (dist == 0) {
                return ActionStatus.ALREADY_COMPLETED;
            }
        }
        System.err.println("Invalid game move: ");
        System.err.println(location + "; " + location.dist(t) + "; " + actionPoints);
        return ActionStatus.IMPOSSIBLE;
    }

}
