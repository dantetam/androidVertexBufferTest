package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.Collection;
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
    private List<Item> inventory;
    //public
    public static int globalIdCounter = 0;
    public int id;
    public World world;

    public int health, maxHealth;

    public List<Action> actionsQueue; //Do actions at position 0 first
    public int actionPoints, maxActionPoints;

    public int atk, def, fire, shock;
    public List<String> combatAbilities;
    public int exp;

    public double workCompleted, workNeeded;
    public double completionPercentage() {return workCompleted / workNeeded;}

    public int inventorySpace;

    public boolean enabled;

    public Ability specialAbility = null;

    public Entity(World w, Clan c) {
        //locations = new ArrayList<Tile>();
        world = w;
        clan = c;
        inventory = new ArrayList<Item>();
        id = globalIdCounter;
        Entity.globalIdCounter++;
        actionsQueue = new ArrayList<>();
        enabled = true;

        combatAbilities = new ArrayList<>();
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

    public abstract void executeQueue();

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

    public ActionStatus gameExecuteAbility() {
        return specialAbility.gameExecuteAbility(this);
    }

    public void addAllToInventory(Collection<Item> items) {
        for (Item item: items) {
            addToInventory(item);
        }
    }
    public void addAllToInventory(Item[] items) {
        for (Item item: items) {
            addToInventory(item);
        }
    }
    public void addToInventory(Item addItem) {
        changeInventory(addItem, true);
    }
    public void subtractFromInventory(Item addItem) {
        changeInventory(addItem, false);
    }
    private void changeInventory(Item addItem, boolean positive) {
        for (int i = 0; i < inventory.size(); i++) {
            Item item = inventory.get(i);
            if (addItem.type == item.type) {
                if (positive) {
                    item.quantity += addItem.quantity;
                }
                else {
                    item.quantity -= addItem.quantity;
                }
                if (item.quantity == 0) {
                    inventory.remove(i);
                }
                return;
            }
        }
        inventory.add(addItem);
    }
    public List<Item> getInventory() {
        return inventory;
    }

    public boolean hasItemsInInventory(Item[] items, boolean remove) {
        List<Item> inventoryItems = new ArrayList<>();
        for (Item item: items) {
            boolean reqFound = false;
            for (Item inventoryItem: inventory) {
                if (item.equals(inventoryItem) && item.quantity > inventoryItem.quantity) {
                    if (remove) {
                        inventoryItems.add(inventoryItem);
                    }
                    reqFound = true;
                    break;
                }
            }
            if (!reqFound) {
                return false;
            }
        }
        if (remove) {
            for (Item requested: items) {
                //inventoryItems.get(i).quantity -= requested.quantity;
                subtractFromInventory(requested);
            }
        }
        return true;
    }

}
