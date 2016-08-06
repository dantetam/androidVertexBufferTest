package io.github.dantetam.world;

import android.widget.PopupMenu;

import java.util.ArrayList;
import java.util.List;

import io.github.dantetam.opstrykontest.WorldSystem;
import static io.github.dantetam.world.Action.ActionType;
import static io.github.dantetam.world.Action.ActionStatus;

/**
 * Created by Dante on 6/13/2016.
 */
public class Person extends Entity {

    public PersonType personType;

    public int age;
    public List<Tech> skills;

    public Person(World world, Clan clan, String name) {
        super(world, clan);
        clan.people.add(this);
        this.name = name;
        skills = new ArrayList<>();
    }

    public void executeQueue() {
        if (!enabled) {
            return;
        }
        while (true) {
            if (actionsQueue.size() == 0) return;
            Action action = actionsQueue.get(0);
            ActionStatus status = action.execute(this);
            /*if (action.execute() == ActionStatus.ALREADY_COMPLETED || action.execute() == ActionStatus.EXECUTED) {
                actionsQueue.remove(0);
            } else {
                break;
            }*/
            if (status == ActionStatus.CONSUME_UNIT) {
                PersonFactory.removePerson(this);
                return;
            }
            else if (status == ActionStatus.ALREADY_COMPLETED || status == ActionStatus.EXECUTED) {
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
        ActionStatus moved = super.gameMove(t);
        if (moved == ActionStatus.EXECUTED) {
            actionPoints--;
        }
        return moved;
    }

    public ActionStatus gameBuild(Building b) {
        if (b.location() == null) {
            b.move(location);
        }
        if (b.completionPercentage() < 1) {
            if (actionPoints <= 0) {
                return ActionStatus.OUT_OF_ENERGY;
            }
            b.workNeeded += calculateWorkAdded();
            actionPoints--;
            if (b.completionPercentage() < 1) {
                return ActionStatus.CONTINUING;
            }
            else
                return ActionStatus.EXECUTED;
        }
        else {
            return ActionStatus.ALREADY_COMPLETED;
        }
    }

    private double calculateWorkAdded() {
        return 3;
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

    public ActionStatus gameMovePath(Tile destination) {
        List<Tile> path = WorldSystem.worldPathfinder.findPath(location, destination);
        if (path == null) {
            return ActionStatus.IMPOSSIBLE;
        }
        path.remove(0);
        while (true) {
            if (path.size() == 0) {
                return ActionStatus.ALREADY_COMPLETED;
            }
            ActionStatus status = gameMove(path.get(0));
            if (status == ActionStatus.OUT_OF_ENERGY) { //Out of AP
                for (Tile t: path) {
                    actionsQueue.add(new PersonAction(ActionType.MOVE, t));
                }
                return ActionStatus.OUT_OF_ENERGY;
            } else {
                path.remove(0);
            }
        }
    }

    public enum PersonType {
        WARRIOR,
        SETTLER;
    }

}
