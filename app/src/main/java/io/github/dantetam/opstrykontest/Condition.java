package io.github.dantetam.opstrykontest;

/*
Created by Dante on 7/4/2016.
We use this class as a convenient way to impose lambda style statements
on some objects, and we guarantee the existence of the method allowed(Object obj).
*/
public abstract class Condition {
    public abstract boolean allowed(Object obj);
    public void init(int i) {}
    public void init() {}
}
