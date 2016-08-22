package io.github.dantetam.world.action;

import io.github.dantetam.world.entity.Building;
import io.github.dantetam.world.entity.Person;

/**
 * Created by Dante on 7/17/2016.
 */
public class BuildingAction extends Action {

    public BuildingAction(ActionType t, Object obj) {
        super(t, obj);
    }

    public ActionStatus execute(Object object) {
        /*if (!(object instanceof Building)) return ActionStatus.IMPOSSIBLE;
        Building building = (Building) object;
        switch (type) {
            case PROCESS:
                return building.gameProcess();
            case QUEUE_BUILD_MODULE:
                return building.gameBuildModule((Building) data);
            case QUEUE_BUILD_UNIT:
                return building.gameBuildUnit((Person) data);
            default:
                System.out.println("Invalid action type: " + type);
                return ActionStatus.IMPOSSIBLE;
        }*/
        return ActionStatus.IMPOSSIBLE;
    }

    public String toString() {
        switch (type) {
            case PROCESS:
                return "";
            case QUEUE_BUILD_MODULE:
                return "Add: " + ((Building) data).name;
            case QUEUE_BUILD_UNIT:
                return "Build: " +  ((Person) data).name;
            default:
                System.out.println("Invalid action type: " + type);
                return null;
        }
    }

}
