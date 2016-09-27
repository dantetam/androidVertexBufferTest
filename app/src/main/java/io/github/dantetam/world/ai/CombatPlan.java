/*
package io.github.dantetam.world.ai;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.dantetam.world.action.Action;
import io.github.dantetam.world.action.CombatAction;
import io.github.dantetam.world.entity.Building;
import io.github.dantetam.world.entity.Entity;
import io.github.dantetam.world.entity.Person;
import io.github.dantetam.world.entity.Tile;
import io.github.dantetam.world.factory.BuildingFactory;
import io.github.dantetam.world.factory.PersonFactory;

*/
/**
 * Created by Dante on 8/1/2016.
 *//*

public class CombatPlan {

    private LinkedHashMap<Entity, List<CombatAction>> planMap;
    private Tile center;
    private int radius;

    public CombatPlan(Tile c, int r) {
        planMap = new LinkedHashMap<>();
        center = c;
        radius = r;
    }

    public void addAction(Entity entity, CombatAction action) {
        if (!checkTileWithinZone(entity.location())) {
            return;
        }
        List<CombatAction> actions = planMap.get(entity);
        if (actions == null) {
            List<CombatAction> list = new ArrayList<>();
            list.add(action);
            planMap.put(entity, list);
        }
        else {
            actions.add(action);
        }
    }

    public void execute() {
        for (Map.Entry<Entity, List<CombatAction>> entry: planMap.entrySet()) {
            Entity entity = entry.getKey();
            List<CombatAction> actions = entry.getValue();
            */
/*for (CombatAction action: actions) {
                action.execute();
            }*//*

            executeQueue(entity, actions);
        }
    }

    public void executeQueue(Entity entity, List<CombatAction> actionsQueue) {
        while (true) {
            if (actionsQueue.size() == 0) return;
            Action action = actionsQueue.get(0);
            Action.ActionStatus status = action.execute(entity);

            if (status == Action.ActionStatus.CONSUME_UNIT) {
                if (entity instanceof Person) {
                    PersonFactory.removePerson((Person) entity);
                }
                else if (entity instanceof Building) {
                    BuildingFactory.removeBuilding((Building) entity);
                }
                return;
            }
            else if (status == Action.ActionStatus.ALREADY_COMPLETED || status == Action.ActionStatus.EXECUTED) {
                actionsQueue.remove(0);
            }
            else if (status == Action.ActionStatus.IMPOSSIBLE) {
                actionsQueue.remove(0);
                //TODO: Error code? Print info about errant action?
            }
            else if (status == Action.ActionStatus.OUT_OF_ENERGY) {
                break;
            }
            else if (status == Action.ActionStatus.CONTINUING) {
                //do nothing, keep the action in the first slot, it'll be repeated.
            }
        }
    }

    public void clear() {
        planMap.clear();
    }

    public boolean checkTileWithinZone(Tile t) {
        return center.dist(t) <= radius;
    }

}
*/
