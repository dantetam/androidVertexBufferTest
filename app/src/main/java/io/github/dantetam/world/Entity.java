package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dante on 6/13/2016.
 */
public abstract class Entity extends Representable {

    protected Tile location;
    public String name;
    public Clan clan;
    public List<Item> items;

    public Entity(Clan c) {
        //locations = new ArrayList<Tile>();
        clan = c;
        items = new ArrayList<Item>();
    }

    public void move(Tile t) {
        if (t != null) {
            t.occupants.remove(this);
        }
        location = t;
        t.occupants.add(this);
        /*int deltaX = t.row - locations.get(0).row;
        int deltaY = t.col - locations.get(0).col;
        Tile center = locations.remove(0);
        for (int i = 0; i < locations.size(); i++) {
            Tile old = locations.get(i);
            Tile newLocation = world.getTile(old.row + deltaX, old.col + deltaY);
            locations.set(i, newLocation);
        }
        locations.add(0, world.getTile(center.row + deltaX, center.col + deltaY));*/
    }

    public Tile location() {
        return location;
    }

}
