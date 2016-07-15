package io.github.dantetam.opstrykontest;

import android.opengl.GLES20;
import android.support.percent.PercentRelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.dantetam.world.*;

/**
 * Created by Dante on 6/17/2016.
 * A connection between the classes of the world package that store world data,
 * and the OpenGL classes that render the world.
 */
public class WorldHandler {

    public World world;
    public WorldGenerator worldGenerator;

    private MousePicker mousePicker;
    private AssetHelper assetHelper;

    private ListModel tilesStored = null;

    //This block is to be rendered
    public HashMap<Tile.Biome, Solid> storedBiomeTiles; //Store all the hexes grouped by biomes, this way each biome can be rendered with its own texture.
    public Solid storedSelectedTileSolid;
    public Solid storedSelectedUnitSolid;

    public HashMap<Tile, Vector3f> storedTileVertexPositions; //Used for placing future improvements at the center of tiles
    public HashMap<Tile, Solid> storedTileImprovements; //For storing the models placed at the tiles which can change
    public MapModel<Building> improvementsStored;

    public HashMap<Entity, Solid> storedTileUnits;
    public MapModel<Entity> unitsStored;

    public MapModel<Clan> tileHighlightOwnerStored;
    public MapModel<Clan> tileHighlightInfluenceStored;

    //public HashMap<Tile, Polygon> hexesShape; //Originally intended to be used for mouse picking. More efficient to use center vertices.

    private LessonSevenActivity mActivity;
    private LessonSevenRenderer mRenderer;

    static final int POSITION_DATA_SIZE = 3;
    static final int NORMAL_DATA_SIZE = 3;
    static final int TEXTURE_COORDINATE_DATA_SIZE = 2;
    static final int BYTES_PER_FLOAT = 4;

    public WorldHandler(LessonSevenActivity mActivity, LessonSevenRenderer mRenderer, MousePicker mousePicker, AssetHelper assetHelper, int len1, int len2) {
        world = new World(len1, len2);
        worldGenerator = new WorldGenerator(world);
        worldGenerator.init();
        this.mActivity = mActivity;
        this.mRenderer = mRenderer;
        this.mousePicker = mousePicker;
        this.assetHelper = assetHelper;

        storedBiomeTiles = new HashMap<>();
        storedTileVertexPositions = new HashMap<>();
        storedTileImprovements = new HashMap<>();
        storedTileUnits = new HashMap<>();
    }

    /**
     * This generates a new VBO for the world as its concrete representation if necessary,
     * and returns it. The idea is that a new VBO should not be generated every time.
     * TODO: Link tiles to positions? So that it is easy to add and remove model VBOs at certain tiles.
     * @return The new VBO.
     */
    public ListModel worldRep() {
        if (tilesStored == null) {
            tilesStored = new ListModel();
            //hexesShape = new HashMap<>();
            //tilesStored.add(generateHexes(world));
            for (int i = 0; i < Tile.Biome.numBiomes; i++) {
                Condition cond = new Condition() {
                    public int desiredType = 0;
                    public void init(int i) {
                        desiredType = i;
                    }
                    public boolean allowed(Object obj) {
                        if (!(obj instanceof Tile)) return false;
                        Tile t = (Tile) obj;
                        //if (t.equals(mousePicker.selectedTile)) return false;
                        return t.biome.type == desiredType;
                    }
                };
                cond.init(i);
                float[] color = Tile.Biome.colorFromInt(i);
                //float[] color = {(int)(Math.random()*256f), (int)(Math.random()*256f), (int)(Math.random()*256f), 255f};
                //System.out.println(color[0] + " " + color[1] + " " + color[2] + " " + color[3]);
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
            /*LessonSevenRenderer.Condition cond = new LessonSevenRenderer.Condition() {
                public boolean allowed(Object obj) {
                    if (!(obj instanceof Tile)) return false;
                    Tile t = (Tile) obj;
                    return t.equals(mousePicker.selectedTile);
                }
            };
            storedSelectedTileSolid = generateHexes(TextureHelper.loadTexture("usb_android"), world, cond);
            tilesStored.add(storedSelectedTileSolid);*/
        }
        return tilesStored;
    }

    public void tileHighlightRep() {
        if (tileHighlightOwnerStored == null) {
            List<Tile> validTiles = world.getAllValidTiles();
            tileHighlightOwnerStored = new MapModel<>();
            tileHighlightInfluenceStored = new MapModel<>();
            createHighlightRep(tileHighlightOwnerStored, tileHighlightInfluenceStored, validTiles);
            //System.out.println("Update main");
        }
        else if (world.clanTerritoriesUpdate.size() > 0) {
            //TODO: Fix -> createHighlightRep(tileHighlightOwnerStored, tileHighlightInfluenceStored, world.clanTerritoriesUpdate);
            //System.out.println("Update tiles " + world.clanTerritoriesUpdate.size());
        }
        world.clanTerritoriesUpdate.clear();
        //return tileHighlightStored;
    }

    //TODO: Update only at every end of turn?
    private void createHighlightRep(MapModel mapOwner, MapModel mapInfluence, List<Tile> tiles) {
        Object[] data = world.aggregateOwners(tiles);
        HashMap<Clan, List<Tile>> owners = (HashMap<Clan, List<Tile>>) data[0];
        HashMap<Clan, List<Tile>> influencers = (HashMap<Clan, List<Tile>>) data[1];
        List<Tile> neutral = (List<Tile>) data[2];
        for (Map.Entry<Clan, List<Tile>> en: owners.entrySet()) {
            List<Tile> ownerTiles = en.getValue();
            Clan clan = en.getKey();
            Vector4f drawColor = clan.color;
            int textureHandle = ColorTextureHelper.loadColor(drawColor);
            float[][] objData = ObjLoader.loadObjModelByVertex(mActivity, R.raw.hexagonflat);
            final float[] totalCubePositionData = new float[objData[0].length * tiles.size()];
            final float[] totalNormalPositionData = new float[objData[0].length * tiles.size() / POSITION_DATA_SIZE * NORMAL_DATA_SIZE];
            final float[] totalTexturePositionData = new float[objData[0].length * tiles.size() / POSITION_DATA_SIZE * TEXTURE_COORDINATE_DATA_SIZE];

            int posOffset = 0, norOffset = 0, texOffset = 0;
            for (Tile tile: ownerTiles) {
                Vector3f vertices = storedTileVertexPositions.get(tile);

                final float[] scaled = scaleData(objData[0], 0.6f, 0.6f, 0.6f);
                final float[] thisCubePositionData = translateData(scaled, vertices.x, vertices.y + 0.2f, vertices.z);

                System.arraycopy(thisCubePositionData, 0, totalCubePositionData, posOffset, thisCubePositionData.length);
                System.arraycopy(objData[1], 0, totalNormalPositionData, norOffset, objData[1].length);
                System.arraycopy(objData[2], 0, totalTexturePositionData, texOffset, objData[2].length);

                posOffset += thisCubePositionData.length;
                norOffset += objData[1].length;
                texOffset += objData[2].length;
            }
            float[][] improvementData = new float[][]{totalCubePositionData, totalNormalPositionData, totalTexturePositionData};
            Solid solid = ObjLoader.loadSolid(textureHandle, null, improvementData);
            mapOwner.put(clan, solid);
        }
        for (Map.Entry<Clan, List<Tile>> en: influencers.entrySet()) {
            List<Tile> influenceTiles = en.getValue();
            Clan clan = en.getKey();
            Vector4f drawColor = clan.reducedColor;
            int textureHandle = ColorTextureHelper.loadColor(drawColor);
            float[][] objData = ObjLoader.loadObjModelByVertex(mActivity, R.raw.hexagonflat);
            final float[] totalCubePositionData = new float[objData[0].length * tiles.size()];
            final float[] totalNormalPositionData = new float[objData[0].length * tiles.size() / POSITION_DATA_SIZE * NORMAL_DATA_SIZE];
            final float[] totalTexturePositionData = new float[objData[0].length * tiles.size() / POSITION_DATA_SIZE * TEXTURE_COORDINATE_DATA_SIZE];

            //TODO: Make a function to repeat this tile aggregation process
            int posOffset = 0, norOffset = 0, texOffset = 0;
            for (Tile tile: influenceTiles) {
                Vector3f vertices = storedTileVertexPositions.get(tile);

                final float[] scaled = scaleData(objData[0], 0.6f, 0.6f, 0.6f);
                final float[] thisCubePositionData = translateData(scaled, vertices.x, vertices.y + 0.2f, vertices.z);

                System.arraycopy(thisCubePositionData, 0, totalCubePositionData, posOffset, thisCubePositionData.length);
                System.arraycopy(objData[1], 0, totalNormalPositionData, norOffset, objData[1].length);
                System.arraycopy(objData[2], 0, totalTexturePositionData, texOffset, objData[2].length);

                posOffset += thisCubePositionData.length;
                norOffset += objData[1].length;
                texOffset += objData[2].length;
            }
            float[][] improvementData = new float[][]{totalCubePositionData, totalNormalPositionData, totalTexturePositionData};
            Solid solid = ObjLoader.loadSolid(textureHandle, null, improvementData);
            mapInfluence.put(clan, solid);
        }

    }

    public MapModel tileImprovementRep() {
        if (improvementsStored == null)
            updateTileImprovement(world.getAllValidTiles());
        return improvementsStored;
    }

    public MapModel updateTileImprovement(List<Tile> tiles) {
        /*if (improvementsStored == null) {
            improvementsStored = new Model();
        }*/
        if (improvementsStored == null) {
            improvementsStored = new MapModel<>();
        }
        for (Tile tile : tiles) {
            if (tile != null && tile.improvement != null) {
                //Solid improvement = ObjLoader.loadSolid(R.drawable.usb_android, tile.improvement.buildingType.name, assetManager.open(tile.improvement.name + ".obj"));
                float[][] objData = assetHelper.loadVertexFromAssets(tile.improvement.name + ".obj");

                final float[] totalCubePositionData = new float[objData[0].length];
                final float[] totalNormalPositionData = new float[objData[0].length / POSITION_DATA_SIZE * NORMAL_DATA_SIZE];
                final float[] totalTexturePositionData = new float[objData[0].length / POSITION_DATA_SIZE * TEXTURE_COORDINATE_DATA_SIZE];

                Vector3f vertices = storedTileVertexPositions.get(tile);

                final float[] scaledData = scaleData(objData[0], 0.2f, 0.2f, 0.2f);

                final float[] thisCubePositionData = translateData(scaledData, vertices.x, vertices.y, vertices.z);
                System.arraycopy(thisCubePositionData, 0, totalCubePositionData, 0, thisCubePositionData.length);

                System.arraycopy(objData[1], 0, totalNormalPositionData, 0, objData[1].length);
                System.arraycopy(objData[2], 0, totalTexturePositionData, 0, objData[2].length);

                float[][] improvementData = new float[][]{totalCubePositionData, totalNormalPositionData, totalTexturePositionData};
                Solid improvement = ObjLoader.loadSolid(TextureHelper.loadTexture("usb_android", mActivity, R.drawable.usb_android), null, improvementData);

                storedTileImprovements.put(tile, improvement);
            }
        }
        for (Map.Entry<Tile, Solid> en: storedTileImprovements.entrySet()) {
            improvementsStored.put(en.getKey().improvement, en.getValue());
        }
        return improvementsStored;
    }

    public MapModel updateTileUnits(List<Tile> tiles) {
        /*if (improvementsStored == null) {
            improvementsStored = new Model();
        }*/
        //TODO: Impl. the use of this method
        if (unitsStored == null) {
            unitsStored = new MapModel<>();
        }
        for (Tile tile : tiles) {
            if (tile != null && tile.improvement != null) {
                //Solid improvement = ObjLoader.loadSolid(R.drawable.usb_android, tile.improvement.buildingType.name, assetManager.open(tile.improvement.name + ".obj"));
                float[][][] unitsData = new float[tile.occupants.size()][][];
                int totalLength = 0;
                for (int i = 0; i < tile.occupants.size(); i++) {
                    Entity en = tile.occupants.get(i);
                    float[][] unitData = assetHelper.loadVertexFromAssets(en.name + ".obj");
                    unitsData[i] = unitData;
                    totalLength += unitData[0].length;
                }
                final float[] totalCubePositionData = new float[totalLength];
                final float[] totalNormalPositionData = new float[totalLength / POSITION_DATA_SIZE * NORMAL_DATA_SIZE];
                final float[] totalTexturePositionData = new float[totalLength / POSITION_DATA_SIZE * TEXTURE_COORDINATE_DATA_SIZE];
                for (int i = 0; i < tile.occupants.size(); i++) {
                    Entity en = tile.occupants.get(i);
                    float[][] objData = unitsData[i];

                    Vector3f vertices = storedTileVertexPositions.get(tile);

                    final float[] scaledData = scaleData(objData[0], 0.2f, 0.2f, 0.2f);

                    final float[] thisCubePositionData = translateData(scaledData, vertices.x, vertices.y, vertices.z);
                    System.arraycopy(thisCubePositionData, 0, totalCubePositionData, 0, thisCubePositionData.length);

                    System.arraycopy(objData[1], 0, totalNormalPositionData, 0, objData[1].length);
                    System.arraycopy(objData[2], 0, totalTexturePositionData, 0, objData[2].length);

                    float[][] improvementData = new float[][]{totalCubePositionData, totalNormalPositionData, totalTexturePositionData};
                    Solid improvement = ObjLoader.loadSolid(TextureHelper.loadTexture("usb_android", mActivity, R.drawable.usb_android), null, improvementData);

                    storedTileUnits.put(en, improvement);
                }
            }
        }
        for (Map.Entry<Entity, Solid> en: storedTileUnits.entrySet()) {
            unitsStored.put(en.getKey(), en.getValue());
        }
        return unitsStored;
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
        Condition cond = new Condition() {
            public boolean allowed(Object obj) {
                return true;
            }
        };
        return generateHexes(textureHandle, world, cond);
    }

    public Solid pathfinderRep(int textureHandle) {
        if (mousePicker.selectedNeedsUpdating()) {
            mousePicker.nextFrameSelectedNeedsUpdating = false;

            if (mousePicker == null) {
                storedSelectedTileSolid = null;
                return null;
            }
            Tile selected = mousePicker.getSelectedTile();
            if (selected == null)
            {
                storedSelectedTileSolid = null;
                return null;
            }
            if (storedTileVertexPositions.get(selected) == null) {
                storedSelectedTileSolid = null;
                return null;
            }

            //mousePicker.selectedNeedsUpdating = false;

            float[][] hexData = ObjLoader.loadObjModelByVertex(mActivity, R.raw.hexagon);

            //Create some appropriately sized tables which will store preliminary buffer data
            //Combine them all within these pieces of data.
            final float[] totalCubePositionData = new float[hexData[0].length];
            int cubePositionDataOffset = 0;
            final float[] totalNormalPositionData = new float[hexData[0].length / POSITION_DATA_SIZE * NORMAL_DATA_SIZE];
            int cubeNormalDataOffset = 0;
            final float[] totalTexturePositionData = new float[hexData[0].length / POSITION_DATA_SIZE * TEXTURE_COORDINATE_DATA_SIZE];
            int cubeTextureDataOffset = 0;

            final float[] scaledData = scaleData(hexData[0], 1, 0, 1);

            Vector3f selectedPos = storedTileVertexPositions.get(selected);

            final float[] thisCubePositionData = translateData(scaledData, selectedPos.x, 0.1f, selectedPos.z);

            //Interleave all the new vtn data, per hex.
            System.arraycopy(thisCubePositionData, 0, totalCubePositionData, cubePositionDataOffset, thisCubePositionData.length);
            cubePositionDataOffset += thisCubePositionData.length;

            System.arraycopy(hexData[1], 0, totalNormalPositionData, cubeNormalDataOffset, hexData[1].length);
            cubeNormalDataOffset += hexData[1].length;
            System.arraycopy(hexData[2], 0, totalTexturePositionData, cubeTextureDataOffset, hexData[2].length);
            cubeTextureDataOffset += hexData[2].length;

            float[][] tesselatedHexes = new float[][]{totalCubePositionData, totalNormalPositionData, totalTexturePositionData};

            storedSelectedTileSolid = ObjLoader.loadSolid(textureHandle, null, tesselatedHexes);
        }
        if (storedSelectedTileSolid != null) {
            storedSelectedTileSolid.rotate(mRenderer.frames % 360, 0, 1, 0);
        }
        return storedSelectedTileSolid;
    }

    //TODO: Create a method which generalizes this process of getting preliminary centered OBJ data from a file,
    //modifying it by coordinates, and then turning it into a solid.
    //Perhaps in OBJLoader?
    public Solid selectedMarkerRep(int textureHandle) {
        if (mousePicker.selectedNeedsUpdating()) {
            mousePicker.nextFrameSelectedNeedsUpdating = false;

            if (mousePicker == null) {
                storedSelectedTileSolid = null;
                return null;
            }
            Tile selected = mousePicker.getSelectedTile();
            if (selected == null)
            {
                storedSelectedTileSolid = null;
                return null;
            }
            if (storedTileVertexPositions.get(selected) == null) {
                storedSelectedTileSolid = null;
                return null;
            }

            //mousePicker.selectedNeedsUpdating = false;

            float[][] hexData = ObjLoader.loadObjModelByVertex(mActivity, R.raw.hexagon);

            //Create some appropriately sized tables which will store preliminary buffer data
            //Combine them all within these pieces of data.
            final float[] totalCubePositionData = new float[hexData[0].length];
            int cubePositionDataOffset = 0;
            final float[] totalNormalPositionData = new float[hexData[0].length / POSITION_DATA_SIZE * NORMAL_DATA_SIZE];
            int cubeNormalDataOffset = 0;
            final float[] totalTexturePositionData = new float[hexData[0].length / POSITION_DATA_SIZE * TEXTURE_COORDINATE_DATA_SIZE];
            int cubeTextureDataOffset = 0;

            final float[] scaledData = scaleData(hexData[0], 1, 0, 1);

            Vector3f selectedPos = storedTileVertexPositions.get(selected);

            final float[] thisCubePositionData = translateData(scaledData, selectedPos.x, 0.1f, selectedPos.z);

            //Interleave all the new vtn data, per hex.
            System.arraycopy(thisCubePositionData, 0, totalCubePositionData, cubePositionDataOffset, thisCubePositionData.length);
            cubePositionDataOffset += thisCubePositionData.length;

            System.arraycopy(hexData[1], 0, totalNormalPositionData, cubeNormalDataOffset, hexData[1].length);
            cubeNormalDataOffset += hexData[1].length;
            System.arraycopy(hexData[2], 0, totalTexturePositionData, cubeTextureDataOffset, hexData[2].length);
            cubeTextureDataOffset += hexData[2].length;

            float[][] tesselatedHexes = new float[][]{totalCubePositionData, totalNormalPositionData, totalTexturePositionData};

            storedSelectedTileSolid = ObjLoader.loadSolid(textureHandle, null, tesselatedHexes);
        }
        if (storedSelectedTileSolid != null) {
            storedSelectedTileSolid.rotate(mRenderer.frames % 360, 0, 1, 0);
        }
        return storedSelectedTileSolid;
    }

    public Solid selectedUnitMarkerRep(int textureHandle) {
        if (mousePicker.selectedNeedsUpdating()) {
            mousePicker.nextFrameSelectedNeedsUpdating = false;

            if (mousePicker == null) {
                storedSelectedUnitSolid = null;
                return null;
            }
            Entity selected = mousePicker.getSelectedEntity();
            if (selected == null)
            {
                storedSelectedUnitSolid = null;
                return null;
            }
            Tile selectedLocation = selected.location();
            if (storedTileVertexPositions.get(selectedLocation) == null) {
                storedSelectedUnitSolid = null;
                return null;
            }

            //mousePicker.selectedNeedsUpdating = false;

            float[][] hexData = mRenderer.assetHelper.compressIntoFloatData("unitmarker.obj");
            //float[][] hexData = ObjLoader.loadObjModelByVertex(mActivity, R.raw.hexagon);

            //Create some appropriately sized tables which will store preliminary buffer data
            //Combine them all within these pieces of data.
            final float[] totalCubePositionData = new float[hexData[0].length];
            int cubePositionDataOffset = 0;
            final float[] totalNormalPositionData = new float[hexData[0].length / POSITION_DATA_SIZE * NORMAL_DATA_SIZE];
            int cubeNormalDataOffset = 0;
            final float[] totalTexturePositionData = new float[hexData[0].length / POSITION_DATA_SIZE * TEXTURE_COORDINATE_DATA_SIZE];
            int cubeTextureDataOffset = 0;

            //final float[] scaledData = scaleData(hexData[0], 0.3f, 0.3f, 0.3f);

            //Vector3f selectedPos = storedTileVertexPositions.get(selectedLocation);

            //final float[] thisCubePositionData = translateData(scaledData, selectedPos.x, 2f, selectedPos.z + 1.4f);

            final float[] scaledData = scaleData(hexData[0], 0.3f, 0.3f, 0.3f);

            Vector3f selectedPos = storedTileVertexPositions.get(selectedLocation);

            final float[] thisCubePositionData = translateData(scaledData, selectedPos.x, 2f, selectedPos.z + 1.4f);

            //Interleave all the new vtn data, per hex.
            System.arraycopy(thisCubePositionData, 0, totalCubePositionData, cubePositionDataOffset, thisCubePositionData.length);
            System.arraycopy(hexData[1], 0, totalNormalPositionData, cubeNormalDataOffset, hexData[1].length);
            System.arraycopy(hexData[2], 0, totalTexturePositionData, cubeTextureDataOffset, hexData[2].length);

            float[][] tesselatedHexes = new float[][]{totalCubePositionData, totalNormalPositionData, totalTexturePositionData};

            storedSelectedUnitSolid = ObjLoader.loadSolid(textureHandle, null, tesselatedHexes);
        }
        return storedSelectedUnitSolid;
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
    /*private Solid generateImprovements(int textureHandle, World world, LessonSevenRenderer.Condition condition) {
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

                    float[] vertices = {x * TRANSLATE_FACTORX, tile.elevation / 5f, z * TRANSLATE_FACTORZ + extra};
                    storedTileVertexPositions.put(tile, vertices);

                    final float[] thisCubePositionData = translateData(scaledData, vertices[0], vertices[1]/2f, vertices[2]);

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
        Solid hexes = ObjLoader.loadSolid(textureHandle, null, tesselatedHexes);
        return hexes;
    }*/

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
    private Solid generateHexes(int textureHandle, World world, Condition condition) {
        //Load the vtn data of one hex obj
        float[][] hexData = ObjLoader.loadObjModelByVertex(mActivity, R.raw.hexagon);

        //int mRequestedCubeFactor = WORLD_LENGTH;

        //Count the number of hexes needed so that the correct space is allocated
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

        //Create some appropriately sized tables which will store preliminary buffer data
        //Combine them all within these pieces of data.
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

                    //Scale and translate accordingly so everything fits together
                    float extra = x % 2 == 1 ? TRANSLATE_FACTORZ * -0.5f : 0;
                    final float[] scaledData = scaleData(hexData[0], 1, tile.elevation / 5f, 1);

                    //Store these positions for later use when we place tile improvements and such
                    Vector3f vertices = new Vector3f(x * TRANSLATE_FACTORX, tile.elevation / 5f, z * TRANSLATE_FACTORZ + extra);
                    storedTileVertexPositions.put(tile, vertices);

                    final float[] thisCubePositionData = translateData(scaledData, vertices.x, vertices.y/2f, vertices.z);

                    //Interleave all the new vtn data, per hex.
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
        Solid hexes = ObjLoader.loadSolid(textureHandle, null, tesselatedHexes);
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
