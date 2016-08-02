package io.github.dantetam.world;

/**
 * Created by Dante on 8/1/2016.
 */
public class CombatAction extends Action {

    public CombatAction(Action.ActionType t, Object obj) {
        super(t, obj);
    }

    public Action.ActionStatus execute(Object object) {
        if (!(object instanceof Entity)) return Action.ActionStatus.IMPOSSIBLE;
        Entity building = (Entity) object;
        switch (type) {
            case PROCESS:
                return building.gameProcess();
            case QUEUE_BUILD_MODULE:
                return building.gameBuildModule((Building) data);
            case QUEUE_BUILD_PERSON:
                return building.gameBuildUnit((Person) data);
            default:
                System.out.println("Invalid combat action type: " + type);
                return Action.ActionStatus.IMPOSSIBLE;
        }
    }

    /*public String toString() {
        switch (type) {
            case COMBAT_ATTACK:
                return "";
            case COMBAT_MOVE:
                return "";
            default:
                System.out.println("Invalid combat action type: " + type);
                return null;
        }
    }*/

}
