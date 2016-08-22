package io.github.dantetam.opengl;

/**
 * Created by Dante on 8/22/2016.
 */
public class Texture {

    public String fileName;
    public int textureHandle;

    public Texture(String name, int handle) {
        fileName = name;
        textureHandle = handle;
    }

}
