package io.github.dantetam.world;

/**
 * Created by Dante on 8/1/2016.
 */
public class CombatAction extends Action {

    public CombatAction(Action.ActionType t, Object obj) {
        super(t, obj);
    }

    public Action.ActionStatus execute(Object subject) {
        if (!(subject instanceof Entity)) return Action.ActionStatus.IMPOSSIBLE;
        Entity entity = (Entity) subject;
        switch (type) {
            case COMBAT_ATTACK:
                return ActionStatus.EXECUTED;
            case COMBAT_MOVE:
                return ((Person) entity).gameMovePath((Tile) data);
            case COMBAT_CHASE:
                return ActionStatus.EXECUTED;
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
