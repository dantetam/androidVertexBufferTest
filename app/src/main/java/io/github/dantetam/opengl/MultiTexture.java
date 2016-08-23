package io.github.dantetam.opengl;

/**
 * Created by Dante on 8/22/2016.
 */
public class MultiTexture extends Texture {

    public int textureHandle;
    public int numberOfRows = 1;
    public int textureAtlasIndex = 0;

    public int textureHandle1;
    public int numberOfRows1 = 1;
    public int textureAtlasIndex1 = 0;

    public int textureHandle2;
    public int numberOfRows2 = 1;
    public int textureAtlasIndex2 = 0;

    public int textureHandle3;
    public int numberOfRows3 = 1;
    public int textureAtlasIndex3 = 0;

    public int blendMap;

    public MultiTexture(String name, int handle0, int handle1, int handle2, int handle3, int blendMap) {
        this(name, new int[]{handle0, 1, 0, handle1, 1, 0, handle2, 1, 0, handle3, 1, 0}, blendMap);
    }
    public MultiTexture(String name, int[] twelveArray, int blendMapHandle) {
        super(name, twelveArray[0], twelveArray[1], twelveArray[2]);

        textureHandle = twelveArray[0];
        numberOfRows = twelveArray[1];
        textureAtlasIndex = twelveArray[2];

        textureHandle1 = twelveArray[3];
        numberOfRows1 = twelveArray[4];
        textureAtlasIndex1 = twelveArray[5];

        textureHandle2 = twelveArray[6];
        numberOfRows2 = twelveArray[7];
        textureAtlasIndex2 = twelveArray[8];

        textureHandle3 = twelveArray[9];
        numberOfRows3 = twelveArray[10];
        textureAtlasIndex3 = twelveArray[11];

        blendMap = blendMapHandle;
    }

}
