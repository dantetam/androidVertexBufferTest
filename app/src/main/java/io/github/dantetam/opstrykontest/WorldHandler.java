package io.github.dantetam.opstrykontest;

import android.opengl.GLES20;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.dantetam.world.*;

/**
 * Created by Dante on 6/17/2016.
 * A connection between the classes of the world package that store world data,
 * and the OpenGL classes that render the world.
 */
public class WorldHandler {

    public World world;
    public WorldGenerator worldGenerator;

    private Model tilesStored = null;
    public HashMap<Tile.Biome, Solid> storedBiomeTiles;

    private LessonSevenActivity mActivity;

    static final int POSITION_DATA_SIZE = 3;
    static final int NORMAL_DATA_SIZE = 3;
    static final int TEXTURE_COORDINATE_DATA_SIZE = 2;
    static final int BYTES_PER_FLOAT = 4;

    public WorldHandler(LessonSevenActivity mActivity, int len1, int len2) {
        world = new World(len1, len2);
        worldGenerator = new WorldGenerator(world);
        worldGenerator.init();
        this.mActivity = mActivity;
    }

    /**
     * This generates a new VBO for the world as its concrete representation if necessary,
     * and returns it. The idea is that a new VBO should not be generated every time.
     * TODO: Link tiles to positions? So that it is easy to add and remove model VBOs at certain tiles.
     * @return The new VBO.
     */
    public Model worldRep() {
        if (tilesStored == null) {
            tilesStored = new Model();
            storedBiomeTiles = new HashMap<>();
            //tilesStored.add(generateHexes(world));
            for (int i = 0; i < Tile.Biome.numBiomes; i++) {
                LessonSevenRenderer.Condition cond = new LessonSevenRenderer.Condition() {
                    public int desiredType = 0;
                    public void init(int i) {
                        desiredType = i;
                    }
                    public boolean allowed(Object obj) {
                        if (!(obj instanceof Tile)) return false;
                        Tile t = (Tile) obj;
                        return t.biome.type == desiredType;
                    }
                };
                cond.init(i);
                float[] color = Tile.Biome.colorFromInt(i);
                //float[] color = {(int)(Math.random()*256f), (int)(Math.random()*256f), (int)(Math.random()*256f), 255f};
                int textureHandle = ColorTextureHelper.loadColor(color);

                GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);

                //int textureHandle = TextureHelper.loadTexture("usb_android");
                Solid solidsOfBiome = generateHexes(textureHandle, world, cond);
                storedBiomeTiles.put(Tile.Biome.fromInt(i), solidsOfBiome);
                tilesStored.add(solidsOfBiome);
                //tilesStored.add(solidsOfBiome[0]);
                //tilesStored.add(solidsOfBiome[1]);
            }
        }
        return tilesStored;
    }

    private static float tileWidth = 4;
    /*private Model[][] tileRep() {
        if (tilesStored == null) {
            tilesStored = new Model[world.rows][world.cols];
            for (int r = 0; r < world.rows; r++) {
                for (int c = 0; c < world.cols; c++) {
                    Tile tile = world.getTile(r, c);
                    Solid solid = new Solid();
                    solid.move(r * tileWidth, 0.5f * (float) (tile.elevation), c * tileWidth);
                    solid.scale(tileWidth, tile.elevation, tileWidth);
                    solid.rotate(0, 0, 1, 0);
                    solid.color(Tile.Biome.colorFromInt(tile.biome.type));
                    tilesStored[r][c] = new Model();
                    tilesStored[r][c].add(solid);
                }
            }
        }
        return tilesStored;
    }*/

    private Solid generateAllHexes(int textureHandle, World world) {
        LessonSevenRenderer.Condition cond = new LessonSevenRenderer.Condition() {
            public boolean allowed(Object obj) {
                return true;
            }
        };
        return generateHexes(textureHandle, world, cond);
    }

    //Store previously generated data in here.
    public float[][] tesselatedHexes;

    /**
     *
     * @param textureHandle The texture for which this VBO will have
     * @param world The world to represent
     * @param condition Some sort of restriction, if necessary. In this case,
     *                  we check that the tiles are of a certain biome.
     *                  This is so that we render all tiles of the biome,
     *                  under the same texture and VBO.
     * @return A new solid which is a representation of the provided world
     */
    private Solid generateHexes(int textureHandle, World world, LessonSevenRenderer.Condition condition) {
        float[][] hexData = ObjLoader.loadObjModelByVertex(mActivity, R.raw.hexagon);

        //int mRequestedCubeFactor = WORLD_LENGTH;

        int numHexesToRender = 0;
        for (int x = 0; x < world.arrayLengthX; x++) {
            for (int z = 0; z < world.arrayLengthZ; z++) {
                Tile tile = world.getTile(x,z);
                if (tile == null) continue;
                if (condition.allowed(tile)) {
                    numHexesToRender++;
                }
            }
        }

        final float[] totalCubePositionData = new float[hexData[0].length * numHexesToRender];
        int cubePositionDataOffset = 0;
        final float[] totalNormalPositionData = new float[hexData[0].length / POSITION_DATA_SIZE * NORMAL_DATA_SIZE * numHexesToRender];
        int cubeNormalDataOffset = 0;
        final float[] totalTexturePositionData = new float[hexData[0].length / POSITION_DATA_SIZE * TEXTURE_COORDINATE_DATA_SIZE * numHexesToRender];
        int cubeTextureDataOffset = 0;

        final float TRANSLATE_FACTORX = 3.3f;
        final float TRANSLATE_FACTORZ = 4f;

        for (int x = 0; x < world.arrayLengthX; x++) {
            for (int z = 0; z < world.arrayLengthZ; z++) {
                Tile tile = world.getTile(x,z);
                if (tile == null) continue;
                if (condition.allowed(tile)) {
                    tile.elevation = 0;
                    float extra = x % 2 == 1 ? TRANSLATE_FACTORZ * -0.5f : 0;
                    final float[] scaledData = scaleData(hexData[0], 1, tile.elevation / 5f, 1);
                    final float[] thisCubePositionData = translateData(scaledData, x * TRANSLATE_FACTORX, tile.elevation / 10f, z * TRANSLATE_FACTORZ + extra);

                    System.arraycopy(thisCubePositionData, 0, totalCubePositionData, cubePositionDataOffset, thisCubePositionData.length);
                    cubePositionDataOffset += thisCubePositionData.length;

                    System.arraycopy(hexData[1], 0, totalNormalPositionData, cubeNormalDataOffset, hexData[1].length);
                    cubeNormalDataOffset += hexData[1].length;
                    System.arraycopy(hexData[2], 0, totalTexturePositionData, cubeTextureDataOffset, hexData[2].length);
                    cubeTextureDataOffset += hexData[2].length;
                }
            }
            //}
        }

        tesselatedHexes = new float[][]{totalCubePositionData, totalNormalPositionData, totalTexturePositionData};
        Solid hexes = ObjLoader.loadSolid(textureHandle, tesselatedHexes);
        return hexes;
    }

    /**
     * @param data A piece of data "aligned" into vectors of three components
     * @param dx,dy,dz A direction to translate in
     * @return A new set of data where each x point (0, 3, 6...) is translated by dx,
     * y (1, 4, 7...) by dy, and z (2, 5, 8...) by dz.
     */
    private float[] translateData(float[] data, float dx, float dy, float dz) {
        float[] newData = new float[data.length];
        for (int i = 0; i < data.length; i++) {
            if (i % 3 == 0) newData[i] = data[i] + dx;
            else if (i % 3 == 1) newData[i] = data[i] + dy;
            else newData[i] = data[i] + dz;
        }
        return newData;
    }

    /**
     * @param data A piece of data "aligned" into vectors of three components
     * @param dx,dy,dz A vector to scale by
     * @return A new set of data where each x point (0, 3, 6...) is scaled by dx,
     * y (1, 4, 7...) by dy, and z (2, 5, 8...) by dz.
     */
    private float[] scaleData(float[] data, float dx, float dy, float dz) {
        float[] newData = new float[data.length];
        for (int i = 0; i < data.length; i++) {
            if (i % 3 == 0) newData[i] = data[i] * dx;
            else if (i % 3 == 1) newData[i] = data[i] * dy;
            else newData[i] = data[i] * dz;
        }
        return newData;
    }

    /*
    A convenience method for combining lists of objects,
    used originally to combine solids together into one model.
     */
    public List<Object> concat(List<Object> a, List<Object> b) {
        List<Object> combined = new ArrayList<Object>();
        for (Object o: a) combined.add(o);
        for (Object o: b) combined.add(o);
        return combined;
    }

}
