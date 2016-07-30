package io.github.dantetam.opstrykontest;

import android.opengl.GLES20;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.content.res.TypedArrayUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collection;
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
    private ChunkHelper chunkHelper;

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

    public Solid storedPathSolid;

    public MapModel<Clan> tileHighlightOwnerStored;
    public MapModel<Clan> tileHighlightInfluenceStored;

    public MapModel<Clan> tileTerritoryStored;
    public MapModel<int[]> tileYieldUiStored;

    public RenderEntity highlightedCityTerritory;

    public MapModel<ItemType> improvementResourceProductionUi;
    public MapModel<Condition> improvementResourceStatUi;

    //public HashMap<Tile, Polygon> hexesShape; //Originally intended to be used for mouse picking. More efficient to use center vertices.

    private LessonSevenActivity mActivity;
    private LessonSevenRenderer mRenderer;

    static final int POSITION_DATA_SIZE = 3;
    static final int NORMAL_DATA_SIZE = 3;
    static final int TEXTURE_COORDINATE_DATA_SIZE = 2;
    static final int BYTES_PER_FLOAT = 4;

    public Collection<ChunkHelper.Node> chunkNodes;
    public int oldIdSum = 0;
    public List<Tile> chunkTiles;
    public boolean chunksUpdated = false;

    public WorldHandler(LessonSevenActivity mActivity, LessonSevenRenderer mRenderer, MousePicker mousePicker, AssetHelper assetHelper, ChunkHelper chunkHelper, int len1, int len2) {
        world = new World(len1, len2);
        worldGenerator = new WorldGenerator(world);
        worldGenerator.init();
        this.mActivity = mActivity;
        this.mRenderer = mRenderer;
        this.mousePicker = mousePicker;
        this.assetHelper = assetHelper;
        this.chunkHelper = chunkHelper;

        storedBiomeTiles = new HashMap<>();
        storedTileVertexPositions = new HashMap<>();
        storedTileImprovements = new HashMap<>();
        storedTileUnits = new HashMap<>();
    }

    public Object[] totalWorldRepresentation() {
        List<BaseModel> modelsToRender = new ArrayList<>();
        List<RenderEntity> solidsToRender = new ArrayList<>();

        Collection<ChunkHelper.Node> nodes = chunkHelper.getChunkNodesContainingTile(mousePicker.centerTile, 1);
        chunksUpdated = false;
        if (chunkNodes == null) {
            chunkNodes = nodes;
        }
        else {
            if (nodes.size() != chunkNodes.size()) {
                chunksUpdated = true;
            }
            else {
                int idSum = 0;
                for (ChunkHelper.Node node: nodes) {
                    idSum += node.id;
                }
                idSum -= oldIdSum;
                oldIdSum = idSum;
                if (idSum != 0) {
                    chunksUpdated = true;
                }
            }
        }

        if (chunkTiles == null) {
            chunkTiles = new ArrayList<>();
        }
        if (chunksUpdated) {
            chunkTiles.clear();
            for (ChunkHelper.Node node: nodes) {
                for (Tile tile: node.tiles) {
                    chunkTiles.add(tile);
                }
            }
        }
        /*chunkTiles = chunkHelper.getChunkTiles(mousePicker.centerTile, 1);
        if (chunkTiles == null) {
            chunkTiles = new ArrayList<>();
        }*/

        /*if (LessonSevenRenderer.frames % 100 == 0) {
            for (int x = 0; x < chunkHelper.alignedTiles.length; x++) {
                for (int z = 0; z < chunkHelper.alignedTiles[0].length; z++) {
                    if (chunkHelper.alignedTiles[x][z].equals(mousePicker.getSelectedTile())) {
                        System.out.print("! ");
                    }
                    else if (chunkTiles.contains(chunkHelper.alignedTiles[x][z])) {
                        System.out.print("X ");
                    }
                    else
                        System.out.print("- ");
                }
                System.out.println();
            }
        }*/

        //mousePicker.passInTileVertices(worldHandler.storedTileVertexPositions);

        mousePicker.passInTileVertices(storedTileVertexPositions);

        modelsToRender.add(worldRep());
        modelsToRender.add(updateTileUnits());
        modelsToRender.add(tileImprovementRep());
        solidsToRender.add(selectedMarkerRep(ColorTextureHelper.loadColor(255, 255, 255, 255)));
        solidsToRender.add(selectedUnitMarkerRep(ColorTextureHelper.loadColor(255, 255, 255, 255)));

        modelsToRender.add(tileTerritoryRep());
        tileHighlightRep();

        if (previousYieldRep == null) {
            updateTileYieldRep();
        }

        //TODO: Convert to IBOs next?

        if (mousePicker.getSelectedTile() != null && mousePicker.getSelectedTile().improvement != null) {
            if (mousePicker.getSelectedTile().improvement.buildingType == BuildingType.CITY) {
                improvementResourceProductionUi = null;
                improvementResourceStatUi = null;

                modelsToRender.add(updateTileYieldRep());
                modelsToRender.add(tileYieldInterface());
                if (highlightedCityTerritory == null) {
                    highlightedCityTerritory = createCityTerritoryRep((City) mousePicker.getSelectedTile().improvement);
                    //System.out.println("yes");
                }
                if (highlightedCityTerritory != null) {
                    solidsToRender.add(highlightedCityTerritory);
                }
            }
            else {
                highlightedCityTerritory = null;

                if (improvementResourceProductionUi == null) {
                    createImprovementResourceRep();
                    modelsToRender.add(improvementResourceProductionUi);
                    modelsToRender.add(improvementResourceStatUi);
                }
            }
        }
        else {
            if (highlightedCityTerritory != null) {
                highlightedCityTerritory = null;
            }
        }

        /*if (highlightedCityTerritory != null) {
            solidsToRender.add(highlightedCityTerritory);
        }*/

        modelsToRender.add(tileHighlightOwnerStored);

        if (!mRenderer.buildingWorldFinished) {
            mRenderer.buildingWorldFinished = true;
            mActivity.runOnUiThread(new Thread() {
                public void run() {
                    Animation anim = AnimationUtils.loadAnimation(mActivity, R.anim.splash_alpha);
                    anim.reset();
                    ImageView splashScreen = (ImageView) mActivity.findViewById(R.id.splash_screen_main);
                    splashScreen.clearAnimation();
                    splashScreen.startAnimation(anim);
                    //mLessonSevenActivity.findViewById(R.id.splash_screen_main).setVisibility(View.INVISIBLE);
                    try {
                        sleep(2500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        splashScreen.setVisibility(View.INVISIBLE);
                    }
                }
            });
            mRenderer.getUserInterfaceReady();
            System.out.println("Done loading.");
        }

        return new Object[]{modelsToRender, solidsToRender};
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

    public MapModel tileTerritoryRep() {
        if (tileTerritoryStored == null) {
            tileTerritoryStored = new MapModel<>();
            //tileHighlightOwnerStored = new MapModel<>();
            //tileHighlightInfluenceStored = new MapModel<>();
            createTerritoryRep(tileTerritoryStored, world.getAllValidTiles());
            //System.out.println("Update main");
        }
        else if (world.clanTerritoriesUpdate.size() > 0 || chunksUpdated) {
            //TODO: Fix -> createHighlightRep(tileHighlightOwnerStored, tileHighlightInfluenceStored, world.clanTerritoriesUpdate);
            //System.out.println("Update tiles " + world.clanTerritoriesUpdate.size());
            //createHighlightRep(tileHighlightOwnerStored, tileHighlightInfluenceStored, world.getAllValidTiles());
        }
        world.clanTerritoriesUpdate.clear();
        return tileTerritoryStored;
    }

    public int[] borderMarkers = {
            R.raw.hexagonhollow1,
            R.raw.hexagonhollow6,
            R.raw.hexagonhollow5,
            R.raw.hexagonhollow4,
            R.raw.hexagonhollow3,
            R.raw.hexagonhollow2
    };
    private float[][][] borderObjData = new float[borderMarkers.length][][];
    public void createTerritoryRep(MapModel model, List<Tile> tiles) {
        Object[] data = world.aggregateOwners(tiles);
        HashMap<Clan, List<Tile>> owners = (HashMap<Clan, List<Tile>>) data[0];
        HashMap<Clan, List<Tile>> influencers = (HashMap<Clan, List<Tile>>) data[1];
        List<Tile> neutral = (List<Tile>) data[2];

        for (int i = 0; i < borderMarkers.length; i++) {
            borderObjData[i] = ObjLoader.loadObjModelByVertex(mActivity, borderMarkers[i]);
        }

        for (Map.Entry<Clan, List<Tile>> en: owners.entrySet()) {
            Clan clan = en.getKey();
            List<Tile> ownerTiles = en.getValue();
            List<Tile> clanTiles = new ArrayList<>();
            for (Tile t: ownerTiles) {
                if (t != null) {
                    clanTiles.add(t);
                }
            }
            for (Tile t: influencers.get(clan)) {
                if (t != null) {
                    clanTiles.add(t);
                }
            }
            //List<Tile> clanTiles = en.getValue();

            int numVertices = 0;
            int posOffset = 0, norOffset = 0, texOffset = 0;
            for (Tile tile: clanTiles) {
                boolean[] neighbors = world.neighborsAreDifferent(tile);
                for (int i = 0; i < neighbors.length; i++) {
                    if (neighbors[i]) {
                        numVertices += borderObjData[i][0].length;
                    }
                }
            }

            final float[] totalCubePositionData = new float[numVertices];
            final float[] totalNormalPositionData = new float[numVertices / POSITION_DATA_SIZE * NORMAL_DATA_SIZE];
            final float[] totalTexturePositionData = new float[numVertices / POSITION_DATA_SIZE * TEXTURE_COORDINATE_DATA_SIZE];

            for (Tile tile: clanTiles) {
                boolean[] neighbors = world.neighborsAreDifferent(tile);

                for (int i = 0; i < neighbors.length; i++) {
                    if (neighbors[i]) {
                        Vector3f vertices = storedTileVertexPositions.get(tile);

                        final float[] scaled = scaleData(borderObjData[i][0], 1f, 1f, 1f);
                        final float[] thisCubePositionData = translateData(scaled, vertices.x, vertices.y + 0.05f, vertices.z);

                        System.arraycopy(thisCubePositionData, 0, totalCubePositionData, posOffset, thisCubePositionData.length);
                        System.arraycopy(borderObjData[i][1], 0, totalNormalPositionData, norOffset, borderObjData[i][1].length);
                        System.arraycopy(borderObjData[i][2], 0, totalTexturePositionData, texOffset, borderObjData[i][2].length);

                        posOffset += thisCubePositionData.length;
                        norOffset += borderObjData[i][1].length;
                        texOffset += borderObjData[i][2].length;
                    }
                }
            }

            float[][] markerData = new float[][]{totalCubePositionData, totalNormalPositionData, totalTexturePositionData};
            Solid solid = ObjLoader.loadSolid(ColorTextureHelper.loadColor(en.getKey().color), null, markerData);
            model.put(en.getKey(), solid);
        }
    }

    public RenderEntity createCityTerritoryRep(City city) {
        if (borderObjData.length == 0 || borderObjData[0] == null) {
            for (int i = 0; i < borderMarkers.length; i++) {
                borderObjData[i] = ObjLoader.loadObjModelByVertex(mActivity, borderMarkers[i]);
            }
        }

        int numVertices = 0;
        int posOffset = 0, norOffset = 0, texOffset = 0;
        boolean[] neighbors = new boolean[6 * city.cityTiles.size()];
        int j = 0;
        for (Tile cityTile: city.cityTiles) {
            for (int i = 0; i < world.neighborDirections.length; i++) {
                Tile neighbor = world.getTile(cityTile.q + world.neighborDirections[i][0], cityTile.r + world.neighborDirections[i][1]);
                if (city.workedTiles.keySet().contains(cityTile) && !city.workedTiles.keySet().contains(neighbor)) {
                    numVertices += borderObjData[i][0].length;
                    neighbors[j*6 + i] = true;
                }
                else {
                    neighbors[j*6 + i] = false;
                }
            }
            j++;
        }

        final float[] totalCubePositionData = new float[numVertices];
        final float[] totalNormalPositionData = new float[numVertices / POSITION_DATA_SIZE * NORMAL_DATA_SIZE];
        final float[] totalTexturePositionData = new float[numVertices / POSITION_DATA_SIZE * TEXTURE_COORDINATE_DATA_SIZE];

        j = 0;
        for (Tile cityTile: city.cityTiles) {
            for (int i = 0; i < 6; i++) {
                if (neighbors[j*6 + i]) {
                    Vector3f vertices = storedTileVertexPositions.get(cityTile);

                    final float[] scaled = scaleData(borderObjData[i][0], 1f, 1f, 1f);
                    final float[] thisCubePositionData = translateData(scaled, vertices.x, vertices.y + 0.15f, vertices.z);

                    System.arraycopy(thisCubePositionData, 0, totalCubePositionData, posOffset, thisCubePositionData.length);
                    System.arraycopy(borderObjData[i][1], 0, totalNormalPositionData, norOffset, borderObjData[i][1].length);
                    System.arraycopy(borderObjData[i][2], 0, totalTexturePositionData, texOffset, borderObjData[i][2].length);

                    posOffset += thisCubePositionData.length;
                    norOffset += borderObjData[i][1].length;
                    texOffset += borderObjData[i][2].length;
                }
            }
            j++;
        }

        float[][] markerData = new float[][]{totalCubePositionData, totalNormalPositionData, totalTexturePositionData};
        //ColorTextureHelper.loadColor(city.clan.color)
        Solid solid = ObjLoader.loadSolid(ColorTextureHelper.loadColor(255,255,255,255), null, markerData);
        return solid;
    }

    public void tileHighlightRep() {
        if (tileHighlightOwnerStored == null) {
            tileHighlightOwnerStored = new MapModel<>();
            tileHighlightInfluenceStored = new MapModel<>();
            createHighlightRep(tileHighlightOwnerStored, tileHighlightInfluenceStored, world.getAllValidTiles());
            //System.out.println("Update main");
        }
        else if (world.clanTerritoriesUpdate.size() > 0 || chunksUpdated) {
            //TODO: Fix -> createHighlightRep(tileHighlightOwnerStored, tileHighlightInfluenceStored, world.clanTerritoriesUpdate);
            //System.out.println("Update tiles " + world.clanTerritoriesUpdate.size());
            //createHighlightRep(tileHighlightOwnerStored, tileHighlightInfluenceStored, world.getAllValidTiles());
        }
        world.clanTerritoriesUpdate.clear();
        //return tileHighlightStored;
    }

    private void createHighlightRep(MapModel mapOwner, MapModel mapInfluence, List<Tile> tiles) {
        Object[] data = world.aggregateOwners(tiles);
        HashMap<Clan, List<Tile>> owners = (HashMap<Clan, List<Tile>>) data[0];
        HashMap<Clan, List<Tile>> influencers = (HashMap<Clan, List<Tile>>) data[1];
        List<Tile> neutral = (List<Tile>) data[2];

        float[][] objData = ObjLoader.loadObjModelByVertex(mActivity, R.raw.hexagonflat);

        for (Map.Entry<Clan, List<Tile>> en: owners.entrySet()) {
            List<Tile> ownerTiles = en.getValue();
            Clan clan = en.getKey();
            Vector4f drawColor = clan.color;
            int textureHandle = ColorTextureHelper.loadColor(drawColor);
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
        if (improvementsStored == null) {
            updateTileImprovement(chunkTiles);
        }
        else {
            List<Tile> tilesToUpdate = new ArrayList<>();
            if (previousImprovements == null) {
                previousImprovements = new HashMap<>();
                for (Tile tile : chunkTiles) {
                    if (tile.improvement != null)
                        previousImprovements.put(tile, tile.improvement);
                }
            } else {
                for (Tile tile : chunkTiles) {
                    if (tile.occupants.size() > 0) {
                        if (previousImprovements.get(tile) == null || !previousImprovements.get(tile).equals(tile.improvement)) {
                            tilesToUpdate.add(tile);
                            previousImprovements.put(tile, tile.improvement);
                        }
                    } else {
                        if (previousImprovements.get(tile) != null) {
                            tilesToUpdate.add(tile);
                            previousImprovements.put(tile, null);
                            //previousUnits.put(tile, tile.occupants.get(0));
                        }
                    }
                }
            }
            updateTileImprovement(tilesToUpdate);
        }
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

    final float[][] imprIconInputOffsets = {
            {-2, -2},
            {-2, -1},
            {-2, 0},
            {-2, 1},
            {-2, 2}
    };
    final float[][] imprIconOutputOffsets = {
            {2, -2},
            {2, -1},
            {2, 0},
            {2, 1},
            {2, 2}
    };
    public void createImprovementResourceRep() {
        improvementResourceStatUi = new MapModel<>();

        List<Condition> inputConditions = new ArrayList<>();
        List<Condition> outputConditions = new ArrayList<>();
        ItemType[] items = ItemType.values();
        for (int i = 0; i < items.length; i++) {
            Condition inputCond = new Condition() {
                public ItemType target;
                public boolean allowedTile(Tile t) {
                    if (t.improvement == null) return false;
                    return t.improvement.containsInput(target);
                }
                public void init(Object object) {
                    target = (ItemType) object;
                }
            };
            inputCond.init(items[i]);
            inputConditions.add(inputCond);

            Condition outputCond = new Condition() {
                public ItemType target;
                public boolean allowedTile(Tile t) {
                    if (t.improvement == null) return false;
                    return t.improvement.containsOutput(target);
                }
                public void init(Object object) {
                    target = (ItemType) object;
                }
            };
            outputCond.init(items[i]);
            outputConditions.add(outputCond);
        }

        float[][] hexData = ObjLoader.loadObjModelByVertex("quad", mActivity, R.raw.quad);

        HashMap<ItemType, Integer> textureHandles = new HashMap<>();
        for (int i = 0; i < items.length; i++) {
            //textureHandles[i] = TextureHelper.loadTexture(mActivity.getResources().getResourceEntryName(textures[i]), mActivity, textures[i]);
            //assetHelper.loadVertexFromAssets(items[i].renderName);
            int resId = mActivity.getResources().getIdentifier(items[i].renderName, "drawable", mActivity.getPackageName());
            textureHandles.put(items[i], TextureHelper.loadTexture(items[i].renderName, mActivity, resId));
        }

        for (int i = 0; i < inputConditions.size(); i++) {
            Condition cond = inputConditions.get(i);
            List<Tile> tilesToRender = new ArrayList<>();
            for (Tile tile : world.getAllValidTiles()) {
                if (cond.allowedTile(tile)) {
                    tilesToRender.add(tile);
                }
            }

            //Create some appropriately sized tables which will store preliminary buffer data
            //Combine them all within these pieces of data.
            final float[] totalCubePositionData = new float[hexData[0].length * tilesToRender.size()];
            int cubePositionDataOffset = 0;
            final float[] totalNormalPositionData = new float[hexData[0].length / POSITION_DATA_SIZE * NORMAL_DATA_SIZE * tilesToRender.size()];
            int cubeNormalDataOffset = 0;
            final float[] totalTexturePositionData = new float[hexData[0].length / POSITION_DATA_SIZE * TEXTURE_COORDINATE_DATA_SIZE * tilesToRender.size()];
            int cubeTextureDataOffset = 0;

            float[] offset = imprIconInputOffsets[i];
            float[] trueOffset = {offset[0] * TRANSLATE_FACTOR_UI_X, offset[1] * TRANSLATE_FACTOR_UI_Z};

            for (Tile tile : tilesToRender) {
                Vector3f vertices = storedTileVertexPositions.get(tile);

                float[] scaledData = scaleData(hexData[0], TRANSLATE_FACTOR_UI_X, 1f, TRANSLATE_FACTOR_UI_Z);
                final float[] thisCubePositionData = translateData(scaledData, vertices.x + trueOffset[0], vertices.y + 0.5f, vertices.z + trueOffset[1]);

                //Interleave all the new vtn data, per hex.
                System.arraycopy(thisCubePositionData, 0, totalCubePositionData, cubePositionDataOffset, thisCubePositionData.length);
                cubePositionDataOffset += thisCubePositionData.length;

                System.arraycopy(hexData[1], 0, totalNormalPositionData, cubeNormalDataOffset, hexData[1].length);
                cubeNormalDataOffset += hexData[1].length;
                System.arraycopy(hexData[2], 0, totalTexturePositionData, cubeTextureDataOffset, hexData[2].length);
                cubeTextureDataOffset += hexData[2].length;
            }

            float[][] generatedData = new float[][]{totalCubePositionData, totalNormalPositionData, totalTexturePositionData};
            Solid hexes = ObjLoader.loadSolid(R.drawable.iron_ore, null, generatedData);
            hexes.alphaEnabled = true;

            improvementResourceStatUi.put(cond, hexes);
        }
        for (int i = 0; i < outputConditions.size(); i++) {
            Condition cond = outputConditions.get(i);
            List<Tile> tilesToRender = new ArrayList<>();
            for (Tile tile : world.getAllValidTiles()) {
                if (cond.allowedTile(tile)) {
                    tilesToRender.add(tile);
                }
            }

            //Create some appropriately sized tables which will store preliminary buffer data
            //Combine them all within these pieces of data.
            final float[] totalCubePositionData = new float[hexData[0].length * tilesToRender.size()];
            int cubePositionDataOffset = 0;
            final float[] totalNormalPositionData = new float[hexData[0].length / POSITION_DATA_SIZE * NORMAL_DATA_SIZE * tilesToRender.size()];
            int cubeNormalDataOffset = 0;
            final float[] totalTexturePositionData = new float[hexData[0].length / POSITION_DATA_SIZE * TEXTURE_COORDINATE_DATA_SIZE * tilesToRender.size()];
            int cubeTextureDataOffset = 0;

            float[] offset = imprIconOutputOffsets[i];
            float[] trueOffset = {offset[0] * TRANSLATE_FACTOR_UI_X, offset[1] * TRANSLATE_FACTOR_UI_Z};

            for (Tile tile : tilesToRender) {
                Vector3f vertices = storedTileVertexPositions.get(tile);

                float[] scaledData = scaleData(hexData[0], TRANSLATE_FACTOR_UI_X, 1f, TRANSLATE_FACTOR_UI_Z);
                final float[] thisCubePositionData = translateData(scaledData, vertices.x + trueOffset[0], vertices.y + 0.5f, vertices.z + trueOffset[1]);

                //Interleave all the new vtn data, per hex.
                System.arraycopy(thisCubePositionData, 0, totalCubePositionData, cubePositionDataOffset, thisCubePositionData.length);
                cubePositionDataOffset += thisCubePositionData.length;

                System.arraycopy(hexData[1], 0, totalNormalPositionData, cubeNormalDataOffset, hexData[1].length);
                cubeNormalDataOffset += hexData[1].length;
                System.arraycopy(hexData[2], 0, totalTexturePositionData, cubeTextureDataOffset, hexData[2].length);
                cubeTextureDataOffset += hexData[2].length;
            }

            float[][] generatedData = new float[][]{totalCubePositionData, totalNormalPositionData, totalTexturePositionData};
            Solid hexes = ObjLoader.loadSolid(textureHandles.get(items[i]), null, generatedData);
            hexes.alphaEnabled = true;

            improvementResourceStatUi.put(cond, hexes);
        }
    }

    public ListModel previousYieldRep;
    public HashMap<Tile, Integer> previousTileFood, previousTileProduction, previousTileScience, previousTileGold;
    int[] tileYieldTextures = {R.drawable.food, R.drawable.production, R.drawable.science, R.drawable.gold, R.drawable.population, R.drawable.usb_android, R.drawable.usb_android};
    public ListModel updateTileYieldRep() {
        if (previousTileFood == null) {
            previousTileFood = new HashMap<>();
            previousTileProduction = new HashMap<>();
            previousTileScience = new HashMap<>();
            previousTileGold = new HashMap<>();
            for (Tile tile: world.getAllValidTiles()) {
                previousTileFood.put(tile, tile.food);
                previousTileProduction.put(tile, tile.production);
                previousTileScience.put(tile, tile.science);
                previousTileGold.put(tile, tile.capital);
            }
            updateTileYieldRep(world.getAllValidTiles());
        }
        else {
            List<Tile> tilesToUpdate = new ArrayList<>();
            for (Tile tile: world.getAllValidTiles()) {
                if (previousTileFood.get(tile) != tile.food ||
                        previousTileProduction.get(tile) != tile.production ||
                        previousTileScience.get(tile) != tile.science ||
                        previousTileGold.get(tile) != tile.capital) {
                    tilesToUpdate.add(tile);
                    previousTileFood.put(tile, tile.food);
                    previousTileProduction.put(tile, tile.production);
                    previousTileScience.put(tile, tile.science);
                    previousTileGold.put(tile, tile.capital);
                }
            }
            updateTileYieldRep(tilesToUpdate);
        }
        return previousYieldRep;
    }

    final float TRANSLATE_FACTOR_UI_X = 0.66f;
    final float TRANSLATE_FACTOR_UI_Z = 0.66f;
    final float[][] offsets = {
            new float[]{-2.5f, -1},
            new float[]{-1.5f, -1},
            new float[]{-2.5f, 0},
            new float[]{-1.5f, 0},
            new float[]{0, 2},
            new float[]{0, -2},
            new float[]{2, 0},
    };
    final float[][] numOffsets = {
            new float[]{-3f, -1.5f},
            new float[]{-2f, -1.5f},
            new float[]{-3f, -0.5f},
            new float[]{-2f, -0.5f},
            new float[]{-0.5f, 1.5f},
            new float[]{0, -1.5f},
            new float[]{1.5f, 0.0f},
    };
    public ListModel updateTileYieldRep(List<Tile> tiles) {
        if (previousYieldRep == null) {
            previousYieldRep = new ListModel();
        }

        if (tiles.size() == 0) {
            return previousYieldRep;
        }

        Condition condition1 = new Condition() {
            public boolean allowedTile(Tile t) {
                return t.food > 0;
            }
        };

        Condition condition2 = new Condition() {
            public boolean allowedTile(Tile t) {
                return t.production > 0;
            }
        };

        Condition condition3 = new Condition() {
            public boolean allowedTile(Tile t) {
                return t.science > 0;
            }
        };

        Condition condition4 = new Condition() {
            public boolean allowedTile(Tile t) {
                return t.capital > 0;
            }
        };

        Condition condition5 = new Condition() {
            public boolean allowedTile(Tile t) {
                return t.improvement != null && t.improvement.buildingType == BuildingType.CITY && ((City) t.improvement).population > 0;
            }
        };

        Condition condition6 = new Condition() {
            public boolean allowedTile(Tile t) {
                return t.improvement != null;
            }
        };

        Condition[] conditions = {condition1, condition2, condition3, condition4, condition5, condition6};

        //int[][] textureTints = {{0, 255, 0}, {255, 0, 0}, {0, 0, 255}, {255, 255, 0}};
        int[] textureHandles = new int[tileYieldTextures.length];
        for (int i = 0; i < textureHandles.length; i++) {
            textureHandles[i] = TextureHelper.loadTexture(mActivity.getResources().getResourceEntryName(tileYieldTextures[i]), mActivity, tileYieldTextures[i]);
        }

        for (int i = 0; i < conditions.length; i++) {
            float[][] hexData = ObjLoader.loadObjModelByVertex("quad", mActivity, R.raw.quad);

            Condition cond = conditions[i];
            List<Tile> tilesToRender = new ArrayList<>();
            for (Tile tile : tiles) {
                if (cond.allowedTile(tile)) {
                    tilesToRender.add(tile);
                }
            }

            //Create some appropriately sized tables which will store preliminary buffer data
            //Combine them all within these pieces of data.
            final float[] totalCubePositionData = new float[hexData[0].length * tilesToRender.size()];
            int cubePositionDataOffset = 0;
            final float[] totalNormalPositionData = new float[hexData[0].length / POSITION_DATA_SIZE * NORMAL_DATA_SIZE * tilesToRender.size()];
            int cubeNormalDataOffset = 0;
            final float[] totalTexturePositionData = new float[hexData[0].length / POSITION_DATA_SIZE * TEXTURE_COORDINATE_DATA_SIZE * tilesToRender.size()];
            int cubeTextureDataOffset = 0;

            float[] offset = offsets[i];
            float[] trueOffset = {offset[0] * TRANSLATE_FACTOR_UI_X, offset[1] * TRANSLATE_FACTOR_UI_Z};

            for (Tile tile : tilesToRender) {
                Vector3f vertices = storedTileVertexPositions.get(tile);

                float[] scaledData = scaleData(hexData[0], TRANSLATE_FACTOR_UI_X, 1f, TRANSLATE_FACTOR_UI_Z);
                final float[] thisCubePositionData = translateData(scaledData, vertices.x + trueOffset[0], vertices.y + 0.5f, vertices.z + trueOffset[1]);

                //Interleave all the new vtn data, per hex.
                System.arraycopy(thisCubePositionData, 0, totalCubePositionData, cubePositionDataOffset, thisCubePositionData.length);
                cubePositionDataOffset += thisCubePositionData.length;

                System.arraycopy(hexData[1], 0, totalNormalPositionData, cubeNormalDataOffset, hexData[1].length);
                cubeNormalDataOffset += hexData[1].length;
                System.arraycopy(hexData[2], 0, totalTexturePositionData, cubeTextureDataOffset, hexData[2].length);
                cubeTextureDataOffset += hexData[2].length;
            }

            float[][] generatedData = new float[][]{totalCubePositionData, totalNormalPositionData, totalTexturePositionData};
            Solid hexes = ObjLoader.loadSolid(textureHandles[i], null, generatedData);
            if (i >= 0 && i <= 4)
                hexes.alphaEnabled = true;

            previousYieldRep.add(hexes);
        }
        return previousYieldRep;
    }

    public MapModel<Condition> updateTileResourceRep() {
        MapModel<Condition> model = new MapModel<>();
        ItemType[] items = ItemType.values();
        HashMap<ItemType, Integer> resourceTextureHandles = new HashMap<>();
        List<Condition> resourceConditions = new ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            //textureHandles[i] = TextureHelper.loadTexture(mActivity.getResources().getResourceEntryName(textures[i]), mActivity, textures[i]);
            //assetHelper.loadVertexFromAssets(items[i].renderName);
            int resId = mActivity.getResources().getIdentifier(items[i].renderName, "drawable", mActivity.getPackageName());
            resourceTextureHandles.put(items[i], TextureHelper.loadTexture(items[i].renderName, mActivity, resId));

            Condition cond = new Condition() {
                public ItemType type;
                public boolean allowedTile(Tile t) {
                    return t.resources.size() > 0 && !t.resources.get(0).equals(ItemType.NO_RESOURCE);
                }
                public void init(Object obj) {
                    type = (ItemType) obj;
                }
            };
            cond.init(items[i]);
            resourceConditions.add(cond);
        }

        float[] offset = {2,2};
        float[] trueOffset = {offset[0]*TRANSLATE_FACTOR_UI_X, offset[1]*TRANSLATE_FACTOR_UI_Z};
        int i = 0;
        for (Condition cond: resourceConditions) {

            List<Tile> tilesToRender = new ArrayList<>();
            for (Tile tile: world.getAllValidTiles()) {
                if (cond.allowedTile(tile)) {
                    tilesToRender.add(tile);
                }
            }

            float[][] hexData = ObjLoader.loadObjModelByVertex(mActivity, R.raw.quad);
            //Count the number of hexes needed so that the correct space is allocated
            int numHexesToRender = tilesToRender.size();
            //Create some appropriately sized tables which will store preliminary buffer data
            //Combine them all within these pieces of data.
            final float[] totalCubePositionData = new float[hexData[0].length * numHexesToRender];
            int cubePositionDataOffset = 0;
            final float[] totalNormalPositionData = new float[hexData[0].length / POSITION_DATA_SIZE * NORMAL_DATA_SIZE * numHexesToRender];
            int cubeNormalDataOffset = 0;
            final float[] totalTexturePositionData = new float[hexData[0].length / POSITION_DATA_SIZE * TEXTURE_COORDINATE_DATA_SIZE * numHexesToRender];
            int cubeTextureDataOffset = 0;
            for (Tile tile : tilesToRender) {
                Vector3f vertices = storedTileVertexPositions.get(tile);

                float[] scaledData = scaleData(hexData[0], 0.66f, 1f, 0.66f);
                final float[] thisCubePositionData = translateData(scaledData, vertices.x + trueOffset[0], vertices.y + 0.6f, vertices.z + trueOffset[1]);

                //Interleave all the new vtn data, per hex.
                System.arraycopy(thisCubePositionData, 0, totalCubePositionData, cubePositionDataOffset, thisCubePositionData.length);
                cubePositionDataOffset += thisCubePositionData.length;

                System.arraycopy(hexData[1], 0, totalNormalPositionData, cubeNormalDataOffset, hexData[1].length);
                cubeNormalDataOffset += hexData[1].length;
                System.arraycopy(hexData[2], 0, totalTexturePositionData, cubeTextureDataOffset, hexData[2].length);
                cubeTextureDataOffset += hexData[2].length;
            }
            int resId = resourceTextureHandles.get(items[i]);
            int textureHandle = TextureHelper.loadTexture(items[i].getAndroidResourceName(), mActivity, resourceTextureHandles.get(items[i]));
            float[][] generatedData = new float[][]{totalCubePositionData, totalNormalPositionData, totalTexturePositionData};

            Solid hexes = ObjLoader.loadSolid(textureHandle, null, generatedData);
            //hexes.alphaEnabled = true;
            model.put(cond, hexes);
            i++;
        }
        return model;
    }

    public MapModel updateTileUnits() {
        List<Tile> tilesToUpdate = new ArrayList<>();
        if (previousUnits == null) {
            previousUnits = new HashMap<>();
            for (Tile tile: chunkTiles) {
                if (tile.occupants.size() > 0)
                    previousUnits.put(tile, tile.occupants.get(0));
            }
        }
        else {
            for (Tile tile : chunkTiles) {
                if (tile.occupants.size() > 0) {
                    if (previousUnits.get(tile) == null || !previousUnits.get(tile).equals(tile.occupants.get(0))) {
                        tilesToUpdate.add(tile);
                        previousUnits.put(tile, tile.occupants.get(0));
                    }
                }
                else {
                    if (previousUnits.get(tile) != null) {
                        tilesToUpdate.add(tile);
                        previousUnits.put(tile, null);
                        //previousUnits.put(tile, tile.occupants.get(0));
                    }
                }
            }
        }
        if (unitsStored == null) {
            updateTileUnits(chunkTiles);
        }
        else {
            updateTileUnits(tilesToUpdate);
        }
        return unitsStored;
    }

    private int[] yieldUiStatMarkers = {
            R.drawable.ui_0,
            R.drawable.ui_1,
            R.drawable.ui_2,
            R.drawable.ui_3,
            R.drawable.ui_4,
            R.drawable.ui_5,
            R.drawable.ui_6,
            R.drawable.ui_7,
            R.drawable.ui_8,
            R.drawable.ui_9
    };
    private static float TRANSLATE_FACTOR_UI_NUM_X = 0.5f, TRANSLATE_FACTOR_UI_NUM_Z = 0.5f;
    public MapModel<int[]> tileYieldInterface() {
        if (tileYieldUiStored == null) {
            tileYieldUiStored = new MapModel<>();

            Condition variableCond = new Condition() {
                int type = 0, num = 0;

                public void init(int i, int j) {
                    type = i;
                    num = j;
                }
                public boolean allowedTile(Tile tile) {
                    if (type == 0) {
                        return tile.food == num;
                    } else if (type == 1) {
                        return tile.production == num;
                    } else if (type == 2) {
                        return tile.science == num;
                    } else if (type == 3) {
                        return tile.capital == num;
                    } else if (type == 4) {
                        return tile.improvement != null && tile.improvement.buildingType == BuildingType.CITY && ((City) tile.improvement).population == num;
                    } else {
                        throw new IllegalArgumentException("Invalid type in variable condition");
                    }
                }
            };

            for (int i = 0; i <= 4; i++) {
                float[] offset = numOffsets[i];
                float[] trueOffset = {offset[0]*TRANSLATE_FACTOR_UI_X, offset[1]*TRANSLATE_FACTOR_UI_Z};
                for (int j = 1; j <= 9; j++) {
                    variableCond.init(i, j);
                    List<Tile> tilesToRender = new ArrayList<>();
                    for (Tile tile: world.getAllValidTiles()) {
                        if (variableCond.allowedTile(tile)) {
                            tilesToRender.add(tile);
                        }
                    }

                    float[][] hexData = ObjLoader.loadObjModelByVertex(mActivity, R.raw.quad);
                    //Count the number of hexes needed so that the correct space is allocated
                    int numHexesToRender = tilesToRender.size();
                    //Create some appropriately sized tables which will store preliminary buffer data
                    //Combine them all within these pieces of data.
                    final float[] totalCubePositionData = new float[hexData[0].length * numHexesToRender];
                    int cubePositionDataOffset = 0;
                    final float[] totalNormalPositionData = new float[hexData[0].length / POSITION_DATA_SIZE * NORMAL_DATA_SIZE * numHexesToRender];
                    int cubeNormalDataOffset = 0;
                    final float[] totalTexturePositionData = new float[hexData[0].length / POSITION_DATA_SIZE * TEXTURE_COORDINATE_DATA_SIZE * numHexesToRender];
                    int cubeTextureDataOffset = 0;
                    for (Tile tile : tilesToRender) {
                        Vector3f vertices = storedTileVertexPositions.get(tile);

                        float[] scaledData = scaleData(hexData[0], 0.66f, 1f, 0.66f);
                        final float[] thisCubePositionData = translateData(scaledData, vertices.x + trueOffset[0], vertices.y + 0.6f, vertices.z + trueOffset[1]);

                        //Interleave all the new vtn data, per hex.
                        System.arraycopy(thisCubePositionData, 0, totalCubePositionData, cubePositionDataOffset, thisCubePositionData.length);
                        cubePositionDataOffset += thisCubePositionData.length;

                        System.arraycopy(hexData[1], 0, totalNormalPositionData, cubeNormalDataOffset, hexData[1].length);
                        cubeNormalDataOffset += hexData[1].length;
                        System.arraycopy(hexData[2], 0, totalTexturePositionData, cubeTextureDataOffset, hexData[2].length);
                        cubeTextureDataOffset += hexData[2].length;
                    }
                    int textureHandle = TextureHelper.loadTexture("ui_" + j, mActivity, yieldUiStatMarkers[j]);
                    float[][] generatedData = new float[][]{totalCubePositionData, totalNormalPositionData, totalTexturePositionData};
                    Solid hexes = ObjLoader.loadSolid(textureHandle, null, generatedData);
                    hexes.alphaEnabled = true;
                    tileYieldUiStored.put(new int[]{i,j}, hexes);

                }
            }

        }
        return tileYieldUiStored;
    }

    public HashMap<Tile, Entity> previousUnits = null;
    public HashMap<Tile, Building> previousImprovements = null;

    private MapModel updateTileUnits(List<Tile> tiles) {
        /*if (improvementsStored == null) {
            improvementsStored = new Model();
        }*/
        if (unitsStored == null) {
            unitsStored = new MapModel<>();
        }
        for (Tile tile : tiles) {
            if (tile != null) {
                if (tile.occupants.size() == 0) {
                    //storedTileUnits.put(tile, null);
                }
                else {
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

                        final float[] scaledData = scaleData(objData[0], 1f, 1f, 1f);

                        final float[] thisCubePositionData = translateData(scaledData, vertices.x, vertices.y + 0.3f, vertices.z);
                        System.arraycopy(thisCubePositionData, 0, totalCubePositionData, 0, thisCubePositionData.length);

                        System.arraycopy(objData[1], 0, totalNormalPositionData, 0, objData[1].length);
                        System.arraycopy(objData[2], 0, totalTexturePositionData, 0, objData[2].length);

                        float[][] improvementData = new float[][]{totalCubePositionData, totalNormalPositionData, totalTexturePositionData};
                        Solid improvement = ObjLoader.loadSolid(TextureHelper.loadTexture("usb_android", mActivity, R.drawable.usb_android), null, improvementData);

                        unitsStored.put(en, improvement);
                        //storedTileUnits.put(en, improvement);
                    }
                }
            }
        }
        /*for (Map.Entry<Entity, Solid> en: storedTileUnits.entrySet()) {
            unitsStored.put(en.getKey(), en.getValue());
        }*/
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
        if (storedPathSolid == null || mousePicker.pathNeedsUpdating) {
            mousePicker.pathNeedsUpdating = false;
            final List<Tile> path = mousePicker.path;
            if (path == null) return null;

            float[][] hexData = ObjLoader.loadObjModelByVertex(mActivity, R.raw.hexagonflat);

            Condition condition = new Condition() {
                public boolean allowed(Object obj) {
                    if (!(obj instanceof Tile)) return false;
                    return path.contains((Tile) obj);
                }
            };

            //Count the number of hexes needed so that the correct space is allocated
            int numHexesToRender = path.size();

            //Create some appropriately sized tables which will store preliminary buffer data
            //Combine them all within these pieces of data.
            final float[] totalCubePositionData = new float[hexData[0].length * numHexesToRender];
            int cubePositionDataOffset = 0;
            final float[] totalNormalPositionData = new float[hexData[0].length / POSITION_DATA_SIZE * NORMAL_DATA_SIZE * numHexesToRender];
            int cubeNormalDataOffset = 0;
            final float[] totalTexturePositionData = new float[hexData[0].length / POSITION_DATA_SIZE * TEXTURE_COORDINATE_DATA_SIZE * numHexesToRender];
            int cubeTextureDataOffset = 0;

            for (Tile tile : path) {
                if (tile == null) continue;
                if (condition.allowed(tile)) {
                    Vector3f vertices = storedTileVertexPositions.get(tile);

                    float[] scaledData = scaleData(hexData[0], 0.4f, 1f, 0.4f);
                    final float[] thisCubePositionData = translateData(scaledData, vertices.x, vertices.y + 0.3f, vertices.z);

                    //Interleave all the new vtn data, per hex.
                    System.arraycopy(thisCubePositionData, 0, totalCubePositionData, cubePositionDataOffset, thisCubePositionData.length);
                    cubePositionDataOffset += thisCubePositionData.length;

                    System.arraycopy(hexData[1], 0, totalNormalPositionData, cubeNormalDataOffset, hexData[1].length);
                    cubeNormalDataOffset += hexData[1].length;
                    System.arraycopy(hexData[2], 0, totalTexturePositionData, cubeTextureDataOffset, hexData[2].length);
                    cubeTextureDataOffset += hexData[2].length;
                }
            }

            float[][] generatedData = new float[][]{totalCubePositionData, totalNormalPositionData, totalTexturePositionData};
            Solid hexes = ObjLoader.loadSolid(textureHandle, null, generatedData);
            storedPathSolid = hexes;
        }
        return storedPathSolid;
    }

    //TODO: Create a method which generalizes this process of getting preliminary centered OBJ data from a file,
    //modifying it by coordinates, and then turning it into a solid.
    //Perhaps in OBJLoader?
    public Tile lastSelected = null;
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

            if (selected.equals(lastSelected)) {
                return storedSelectedTileSolid;
            }
            lastSelected = selected;
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

        int xx = 0, zz = 0;
        for (int x = 0; x < world.arrayLengthX; x++) {
            for (int z = 0; z < world.arrayLengthZ; z++) {
                Tile tile = world.getTile(x,z);
                if (tile == null) continue;
                zz++;
                if (condition.allowed(tile)) {
                    tile.elevation = 0;

                    //Scale and translate accordingly so everything fits together
                    float extra = x % 2 == 1 ? TRANSLATE_FACTORZ * -0.5f : 0;
                    final float[] scaledData = scaleData(hexData[0], 1, tile.elevation / 5f, 1);

                    //Store these positions for later use when we place tile improvements and such
                    Vector3f vertices = new Vector3f(xx * TRANSLATE_FACTORX, tile.elevation / 5f, - zz * TRANSLATE_FACTORZ + extra);
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
            xx++;
            zz = 0;
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
