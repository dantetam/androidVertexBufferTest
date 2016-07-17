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
    public int health, maxHealth;

    public Person(World world, Clan clan, String name) {
        super(world, clan);
        clan.people.add(this);
        this.name = name;
        skills = new ArrayList<>();
    }

    public void executeQueue() {
        while (true) {
            if (actionsQueue.size() == 0) return;
            Action action = actionsQueue.get(0);
            ActionStatus status = action.execute();
            /*if (action.execute() == ActionStatus.ALREADY_COMPLETED || action.execute() == ActionStatus.EXECUTED) {
                actionsQueue.remove(0);
            } else {
                break;
            }*/
            if (status == ActionStatus.ALREADY_COMPLETED || status == ActionStatus.EXECUTED) {
                actionsQueue.remove(0);
            }
            else if (status == ActionStatus.IMPOSSIBLE) {
                actionsQueue.remove(0);
                //TODO: Error code? Print info about errant action?
            }
            else if (status == ActionStatus.OUT_OF_ENERGY) {
                break;
            }
            else if (status == ActionStatus.CONTINUING) {
                //do nothing, keep the action in the first slot, it'll be repeated.
            }
        }
    }

    /*
    Move a person within the game by one tile and if the unit has action points.
    Return true if the game move was successful.
     */
    public ActionStatus gameMove(Tile t) {
        Tile location = location();
        if (location != null) {
            float dist = location.dist(t);
            if (actionPoints <= 0) {
                return ActionStatus.OUT_OF_ENERGY;
            }
            if (dist == 1) {
                actionPoints--;
                super.move(t);
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

    public ActionStatus gameBuild(Building b) {
        if (b.completionPercentage < 1) {
            if (actionPoints <= 0) {
                return ActionStatus.OUT_OF_ENERGY;
            }
            b.completionPercentage += 0.2f;
            actionPoints--;
            if (b.completionPercentage < 1) {
                return ActionStatus.CONTINUING;
            }
            return ActionStatus.EXECUTED;
        }
        else {
            return ActionStatus.ALREADY_COMPLETED;
        }
    }

    private ActionStatus gameHealHealth() {
        if (health < maxHealth) {
            if (actionPoints <= 0) {
                return ActionStatus.OUT_OF_ENERGY;
            }
            health += (int) (0.1d * maxHealth);
            if (health > maxHealth) {
                health = maxHealth;
            }
            actionPoints--;
            return ActionStatus.EXECUTED;
        }
        return ActionStatus.ALREADY_COMPLETED;
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
            if (gameMove(path.get(0)) == ActionStatus.OUT_OF_ENERGY) { //Out of AP
                for (Tile t: path) {
                    actionsQueue.add(new PersonAction(ActionType.MOVE, t));
                }
                break;
            } else {
                path.remove(0);
            }
        }
    }

    public class PersonAction extends Person.Action {
        PersonAction(ActionType t, Object obj) {
            super(t, obj);
        }
        public ActionStatus execute() {
            switch (type) {
                case BUILD:
                    return gameBuild((Building) data);
                case MOVE:
                    return gameMove((Tile) data);
                default:
                    System.out.println("Invalid action type: " + type);
                    return ActionStatus.IMPOSSIBLE;
            }
        }
    }


}
