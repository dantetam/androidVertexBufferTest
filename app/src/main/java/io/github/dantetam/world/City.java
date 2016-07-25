package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Dante on 7/25/2016.
 */
public class City extends Building {

    public int population, freeWorkingPopulation;
    public HashMap<Tile, Boolean> workedTiles;
    public Set<Tile> cityTiles;

    public City(World world, Clan clan, BuildingType type, Set<Tile> tiles) {
        super(world, clan, type);
        population = 1;
        freeWorkingPopulation = 1;
        workedTiles = new HashMap<>();
        cityTiles = tiles;
    }

    public void executeQueue() {
        while (true) {
            if (actionsQueue.size() == 0) {
                actionsQueue.add(new BuildingAction(Action.ActionType.PROCESS, this));
            }
            Action action = actionsQueue.get(0);
            Action.ActionStatus status = action.execute(this);

            if (status == Action.ActionStatus.ALREADY_COMPLETED || status == Action.ActionStatus.EXECUTED) {
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

    public Action.ActionStatus gameProcess() {
        if (freeWorkingPopulation > 0) {
            pickBestTiles();
        }
        double food = 0, production = 0, science = 0, capital = 0;
        for (Tile tile: workedTiles.keySet()) {
            food += tile.food;
            production += tile.production;
            science += tile.science;
            capital += tile.capital;
        }
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map)
    {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        } );

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry: list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public void pickBestTiles() {
        //workedTiles.clear();
        freeWorkingPopulation = population - workedTiles.size();
        TreeMap<Tile, Double> scoreTiles = new TreeMap<>();
        for (Tile tile: cityTiles) {
            double score = 0;
            score += tile.food*2 + tile.production*2 + tile.science + tile.capital;
            if (tile.resources.size() > 0) {
                if (tile.resources.get(0).type != ItemType.NO_RESOURCE) {
                    score += tile.resources.size()*3;
                }
            }
            scoreTiles.put(tile, score);
        }
        Map<Tile, Double> sorted = sortByValue(scoreTiles);
        Set<Tile> tilesToPick = sorted.keySet();
        for (Tile tile: tilesToPick) {
            if (freeWorkingPopulation > 0) {
                pickTile(tile);
            }
            else {
                break;
            }
        }
    }

    public boolean pickTile(Tile t) {
        if (workedTiles.get(t) != null && cityTiles.contains(t) && freeWorkingPopulation > 0) {
            workedTiles.put(t, true);
            freeWorkingPopulation--;
            return true;
        }
        return false;
    }

}
