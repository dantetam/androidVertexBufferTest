package io.github.dantetam.world;

import android.widget.PopupMenu;

import java.util.ArrayList;
import java.util.List;

import io.github.dantetam.opstrykontest.WorldSystem;

/**
 * Created by Dante on 6/13/2016.
 */
public class Person extends Entity {

    public PersonType personType;

    public int age;
    public List<Tech> skills;
    public int actionPoints, maxActionPoints;
    public int health, maxHealth;
    public List<Action> actionsQueue; //Do actions at position 0 first

    public Person(World world, Clan clan, String name) {
        super(world, clan);
        clan.people.add(this);
        this.name = name;
        skills = new ArrayList<>();
        actionsQueue = new ArrayList<>();
    }

    public void executeQueue() {
        while (true) {
            if (actionsQueue.size() == 0) return;
            Action action = actionsQueue.get(0);
            if (action.execute()) {
                actionsQueue.remove(0);
            } else {
                break;
            }
        }
    }

    /*
    Move a person within the game by one tile and if the unit has action points.
    Return true if the game move was successful.
     */
    public boolean gameMove(Tile t) {
        Tile location = location();
        if (location != null) {
            if (location.dist(t) == 1) {
                if (actionPoints > 0) {
                    actionPoints--;
                    super.move(t);
                    return true;
                }
            }
        }
        System.err.println("Invalid game move: ");
        System.err.println(location + "; " + location.dist(t) + "; " + actionPoints);
        return false;
    }

    private void gameHealHealth() {
        if (health < maxHealth) {
            health += (int) (0.1d * maxHealth);
            if (health > maxHealth) {
                health = maxHealth;
            }
            actionPoints--;
        }
    }

    public void gameMovePath(Tile destination) {
        List<Tile> path = WorldSystem.worldPathfinder.findPath(location, destination);
        if (path == null) {
            return;
        }
        path.remove(0);
        while (true) {
            if (path.size() == 0) {
                break;
            }
            if (!gameMove(path.get(0))) { //Out of AP
                for (Tile t: path) {
                    actionsQueue.add(new Action(ActionType.MOVE, t));
                }
                break;
            } else {
                path.remove(0);
            }
        }
    }

    public enum ActionType {
        MOVE;
    }
    public class Action {
        public ActionType type;
        public Object data;
        Action(ActionType t, Object obj) {
            type = t;
            data = obj;
        }
        public boolean execute() {
            switch (type) {
                case MOVE:
                    return gameMove((Tile) data);
                default:
                    System.out.println("Invalid action type: " + type);
                    return false;
            }
        }
    }

    public enum PersonType {
        WARRIOR;
    }

}
