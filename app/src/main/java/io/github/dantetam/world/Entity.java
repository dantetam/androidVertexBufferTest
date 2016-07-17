package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dante on 6/13/2016.
 */
public abstract class Entity extends Representable {

    protected Tile location;
    public String name;
    public Clan clan;
    public List<Item> items;
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

    /**
     The action system below works as a queue that the entity inspects each turn.
     The first action is executed and for every action, an action status is returned.
     This denotes what the entity should do next after the action executes (reference ActionStatus enum).
     */

    public enum ActionStatus {
        EXECUTED, //The action was successfully completed, no other data, move on to the next action
        ALREADY_COMPLETED, //This action is a duplicate, move on to the next action
        IMPOSSIBLE, //This action could not be done. Remove it? This is most likely an error in the programmer or the user.
        CONTINUING, //This action will be completed in a different turn. Do not remove it from the queue.
        OUT_OF_ENERGY //Out of action points, stop executing actions
    }

    //These denote various types of actions, which have protocols defined within subclasss of Entity.
    public enum ActionType {
        BUILD,
        MOVE;
    }

    //The representation of the action itself, which contains the type (which chooses the protocol on which to act),
    //as well as the uncasted, raw data, which could be a Tile, Building, Person, etc. Since arrays are instances of Object,
    //it is very fitting that Object[], Object[][], Object[][][]... may all be casted and transferred as Object, temporarily.
    /*
    The general anatomy of an action:

    if (possible) {
        if (alreadyCompleted) return ALREADY_COMPLETED
        if (noActionPoints) return OUT_OF_ENERGY
        else {
            execute action and deduct AP/resources
            return EXECUTED/CONTINUING...
        }
    }
    else {
        return IMPOSSIBLE
    }
    */
    public abstract class Action {
        public ActionType type;
        public Object data;
        Action(ActionType t, Object obj) {
            type = t;
            data = obj;
        }
        abstract ActionStatus execute();
    }

    public enum PersonType {
        WARRIOR;
    }

}
