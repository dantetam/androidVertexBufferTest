package io.github.dantetam.opengl;

/**
 * Created by Dante on 8/22/2016.
 */
public class EightMultiTexture extends Texture {

    public int[] twentyFourArray;

    public int blendMap;

    public EightMultiTexture(String name, int handle0, int handle1, int handle2, int handle3, int blendMap) {
        this(name, new int[]{handle0, 1, 0, handle1, 1, 0, handle2, 1, 0, handle3, 1, 0}, blendMap);
    }
    public EightMultiTexture(String name, int[] twentyFourArray, int blendMapHandle) {
        super(name, 0, 0, 0);

        this.twentyFourArray = twentyFourArray;

        blendMap = blendMapHandle;
    }

}
