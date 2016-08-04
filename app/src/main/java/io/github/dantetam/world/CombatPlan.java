package io.github.dantetam.world;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Dante on 8/1/2016.
 */
public class CombatPlan {

    public LinkedHashMap<Entity, List<CombatAction>> planMap;

    public CombatPlan() {
        planMap = new LinkedHashMap<>();
    }

    public void execute() {

    }

}
