package io.github.dantetam.opengl;

import java.util.Collection;

/**
 * Created by Dante on 7/6/2016.
 */
public interface BaseModel {

    Collection<RenderEntity> parts();
    void release();

}
