package io.github.dantetam.world.entity;

import java.util.List;

/**
 * Created by Dante on 6/23/2016.
 */
public interface Traversable<T> {

    float dist(T t);
    List<T> neighbors();
    boolean equals(Object t);

}
