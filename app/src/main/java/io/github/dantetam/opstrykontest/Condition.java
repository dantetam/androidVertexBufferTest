package io.github.dantetam.opstrykontest;

import io.github.dantetam.world.Tile;

/*
Created by Dante on 7/4/2016.
We use this class as a convenient way to impose lambda style statements
on some objects, and we guarantee the existence of the method allowed(Object obj).
*/
public abstract class Condition {
    public boolean allowed(Object obj) {return false;}
    public boolean allowedTile(Tile t) {return false;}
    public void init(String stringy) {}
    public void init(int i) {}
    public void init() {}
}
