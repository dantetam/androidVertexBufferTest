package io.github.dantetam.world.action;

import io.github.dantetam.world.entity.Person;
import io.github.dantetam.world.entity.Tile;

/**
 * Created by Dante on 7/17/2016.
 */
public class PersonAction extends Action {

    public PersonAction(ActionType t, Object obj) {
        super(t, obj);
    }

    public ActionStatus execute(Object object) {
        if (!(object instanceof Person)) return ActionStatus.IMPOSSIBLE;
        Person person = (Person) object;
        switch (type) {
            /*case BUILD:
                return person.gameBuild((Building) data);*/
            case MOVE:
                return person.gameMove((Tile) data);
            case FORTIFY: //<---TODO
                return person.gameFortify();
            case COMBAT_DEATH:
                return person.consumeUnit();
            default:
                System.out.println("Invalid action type: " + type);
                return ActionStatus.IMPOSSIBLE;
        }
    }

    public String toString() {
        switch (type) {
            /*case BUILD:
                return "Build: " + ((Building) super.data).name;*/
            case MOVE:
                return "Move: " + ((Tile) super.data).toString();
            case FORTIFY:
                return "Fortify";
            default:
                System.out.println("Invalid action type: " + type);
                return null;
        }
    }

}
