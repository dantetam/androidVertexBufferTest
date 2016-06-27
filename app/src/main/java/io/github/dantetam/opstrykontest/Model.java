package io.github.dantetam.opstrykontest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dante on 6/18/2016.
 * This class is a wrapper class for a list of RenderEntity, which are VBOs.
 * This class was originally intended to hold individual cubes and the like,
 * but that approach was too computationally taxing on OpenGL ES,
 * since data of hundreds of vertices had to be transmitted every frame.
 */
public class Model {

    public List<RenderEntity> parts;

    public Model() {
        parts = new ArrayList<RenderEntity>();
    }

    public void add(RenderEntity solid) {
        parts.add(solid);
    }

    public void release() {
        for (RenderEntity solid: parts) {
            solid.release();
        }
    }

}
