package io.github.dantetam.world;

/**
 * Created by Dante on 7/17/2016.
 */
public abstract class Action {
    /**
     The action system below works as a queue that the entity inspects each turn.
     The first action is executed and for every action, an action status is returned.
     This denotes what the entity should do next after the action executes (reference ActionStatus enum).
     */

    public enum ActionStatus {
        EXECUTED, //The action was successfully completed, no other data, move on to the next action
        ALREADY_COMPLETED, //This action is a duplicate, move on to the next action
        IMPOSSIBLE, //This action could not be done. Remove it? This is most likely an error in the programmer or the user.
        CONTINUING, //This action will be completed in a different turn. Do not remove it from the queue.
        OUT_OF_ENERGY, //Out of action points, stop executing actions
        CONSUME_UNIT; //Remove the unit from the game
    }

    //These denote various types of actions, which have protocols defined within subclasss of Entity.
    public enum ActionType {
        //BUILD, //The unit (person) moves to a location and builds
        MOVE, //Move to a tile, one tile at a time
        PROCESS, //The unit (building) converts both generic yield and named resources into other resources
        //FIGHT, //Fight another unit
        QUEUE_BUILD_MODULE, //The unit (building) makes an improvement for itself
        QUEUE_BUILD_UNIT, //The unit (building) creates a unit
        SPECIAL_ABILITY, //Activate one time of the entity's runnable

        COMBAT_MOVE,
        COMBAT_ATTACK,
        COMBAT_CHASE;
    }

    //The representation of the action itself, which contains the type (which chooses the protocol on which to act),
    //as well as the uncasted, raw data, which could be a Tile, Building, Person, etc. Since arrays are instances of Object,
    //it is very fitting that Object[], Object[][], Object[][][]... may all be casted and transferred as Object, temporarily.
    /*
    The general anatomy of an action:

    if (possible) {
        if (alreadyCompleted) return ALREADY_COMPLETED
        if (noActionPoints) return OUT_OF_ENERGY
        else {
            execute action and deduct AP/resources
            return EXECUTED/CONTINUING...
        }
    }
    else {
        return IMPOSSIBLE
    }
    */
    public ActionType type;
    public Object data;
    Action(ActionType t, Object obj) {
        type = t;
        data = obj;
    }
    abstract ActionStatus execute(Object subject);

}
