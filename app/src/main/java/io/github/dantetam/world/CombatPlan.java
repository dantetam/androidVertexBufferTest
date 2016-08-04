package io.github.dantetam.world;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dante on 8/1/2016.
 */
public class CombatPlan {

    public LinkedHashMap<Entity, List<CombatAction>> planMap;

    public CombatPlan() {
        planMap = new LinkedHashMap<>();
    }

    public void execute() {
        for (Map.Entry<Entity, List<CombatAction>> entry: planMap.entrySet()) {
            Entity entity = entry.getKey();
            List<CombatAction> actions = entry.getValue();
            /*for (CombatAction action: actions) {
                action.execute();
            }*/
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

}
