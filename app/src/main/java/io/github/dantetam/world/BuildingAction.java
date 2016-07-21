package io.github.dantetam.world;

/**
 * Created by Dante on 7/17/2016.
 */
public class BuildingAction extends Action {
    public BuildingAction(ActionType t, Object obj) {
        super(t, obj);
    }
    public ActionStatus execute(Object object) {
        if (!(object instanceof Building)) return ActionStatus.IMPOSSIBLE;
        Building building = (Building) object;
        switch (type) {
            case PROCESS:
                building.gameProcess();
            case QUEUE_BUILD_MODULE:
                building.gameBuildModule((Building) data);
            default:
                System.out.println("Invalid action type: " + type);
                return ActionStatus.IMPOSSIBLE;
        }
    }
}
