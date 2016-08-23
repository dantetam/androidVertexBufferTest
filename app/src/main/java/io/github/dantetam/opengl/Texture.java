package io.github.dantetam.opengl;

/**
 * Created by Dante on 8/22/2016.
 */
public class Texture {

    public String fileName;
    public int textureHandle;

    public int numberOfRows = 1;

    public int textureAtlasIndex = 0;

    public Texture(String name, int handle) {
        this(name, handle, 1, 0);
    }
    public Texture(String name, int handle, int numberOfRows, int textureAtlasIndex) {
        fileName = name;
        textureHandle = handle;
        this.numberOfRows = numberOfRows;
        this.textureAtlasIndex = textureAtlasIndex;
    }

    public float getTextureOffsetX() {
        int col = textureAtlasIndex % numberOfRows;
        return (float) col / (float) numberOfRows;
    }
    public float getTextureOffsetY() {
        int row = textureAtlasIndex / numberOfRows;
        return (float) row / (float) numberOfRows;
    }

    public static float getTextureOffsetX(int index, int rows) {
        int col = index % rows;
        return (float) col / (float) rows;
    }
    public static float getTextureOffsetY(int index, int rows) {
        int row = index / rows;
        return (float) row / (float) rows;
    }

}
