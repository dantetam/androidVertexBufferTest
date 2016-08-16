package io.github.dantetam.world;

import java.util.HashMap;

/**
 * Created by Dante on 8/15/2016.
 */
public class BuildingTree {

    public Clan clan;

    public HashMap<String, BuildingType> buildingTypes;

    public BuildingTree(Clan c) {
        clan = c;
        clan.buildingTree = this;
        buildingTypes = new HashMap<>();
    }

}
