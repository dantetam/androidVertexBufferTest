package io.github.dantetam.opstrykontest;

import io.github.dantetam.world.entity.Tile;

/*
Created by Dante on 7/4/2016.
We use this class as a convenient way to impose lambda style statements
on some objects, and we guarantee the existence of the method allowed(Object obj).
*/
public abstract class Condition {
    /*
    Originally these methods were abstract. These are simply here now with unused default
    implementations so that anonymously extending subclasses of Condition
    are not forced to use all methods.
     */
    public boolean allowed(Object obj) {return false;}
    public boolean allowedTile(Tile t) {return false;}

    /*
    These methods are to provide workarounds through the "final" restriction on variables
    manipulated by anonymously extending subclasses. I suppose this is because certain values
    must be known at compile time. These methods pass them in slightly after compile time (i.e. runtime?).
     */
    public void init(String stringy) {}
    public void init(Object object) {}
    public void init(int i) {}
    public void init(int i, int j) {}
    public void init(int a, int b, float c) {}
    public void init() {}
}
