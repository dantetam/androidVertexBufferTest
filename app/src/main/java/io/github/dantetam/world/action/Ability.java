package io.github.dantetam.world.action;

import io.github.dantetam.opstrykontest.Condition;
import io.github.dantetam.world.entity.Building;
import io.github.dantetam.world.entity.BuildingType;
import io.github.dantetam.world.entity.City;
import io.github.dantetam.world.entity.Entity;
import io.github.dantetam.world.entity.Person;
import io.github.dantetam.world.entity.Tile;

/**
 * Created by Dante on 8/1/2016.
 */
public class Ability {

    public String name, desc;

    public Ability(String n, String d) {
        name = n;
        desc = d;
    }

    public Action.ActionStatus gameExecuteAbility(Entity person) {
        return Action.ActionStatus.ALREADY_COMPLETED;
    }

    public class ContainsModuleCondition extends Condition {
        public BuildingType target;
        public void init(Object obj) {target = (BuildingType) obj;}
        public boolean allowed(Object obj) {
            if (!(obj instanceof City)) return false;
            City city = (City) obj;
            for (Building module: city.modules) {
                if (module.buildingType.equals(target)) {
                    return true;
                }
            }
            return false;
        }
    }

    public class BuildingCondition extends Condition {
        public BuildingType target;
        public void init(Object obj) {target = (BuildingType) obj;}
        public boolean allowed(Object obj) {
            return target.equals((BuildingType) obj);
        }
    }

    public class TileCondition extends Condition {
        public Tile.Biome biome;
        public Tile.Terrain terrain;
        public void init(Object obj) {
            if (obj instanceof Tile.Biome) {
                biome = (Tile.Biome) obj;
            } else if (obj instanceof Tile.Terrain) {
                terrain = (Tile.Terrain) obj;
            }
        }
        public void init(Object obj1, Object obj2) {
            init(obj1);
            init(obj2);
        }
        public boolean allowed(Object obj) {
            boolean cond = false;
            if (biome != null) {
                cond = cond || biome.equals((Tile.Biome) obj);
            }
            if (terrain != null) {
                cond = cond || terrain.equals((Tile.Terrain) obj);
            }
            return cond;
        }
    }

}
