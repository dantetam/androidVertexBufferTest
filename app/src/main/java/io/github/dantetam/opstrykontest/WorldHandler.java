package io.github.dantetam.opstrykontest;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import io.github.dantetam.android.AssetHelper;
import io.github.dantetam.android.ColorTextureHelper;
import io.github.dantetam.android.MultiTextureHelper;
import io.github.dantetam.android.ObjLoader;
import io.github.dantetam.android.TerrainTextureHelper;
import io.github.dantetam.android.TextureHelper;
import io.github.dantetam.opengl.BaseModel;
import io.github.dantetam.opengl.ListModel;
import io.github.dantetam.opengl.MapModel;
import io.github.dantetam.opengl.MousePicker;
import io.github.dantetam.opengl.MultiTexture;
import io.github.dantetam.opengl.RenderEntity;
import io.github.dantetam.opengl.Solid;
import io.github.dantetam.opengl.Texture;
import io.github.dantetam.utilmath.Vector2f;
import io.github.dantetam.utilmath.Vector3f;
import io.github.dantetam.utilmath.Vector4f;
import io.github.dantetam.world.entity.Building;
import io.github.dantetam.world.entity.BuildingType;
import io.github.dantetam.world.entity.City;
import io.github.dantetam.world.entity.Clan;
import io.github.dantetam.world.entity.Entity;
import io.github.dantetam.world.entity.ItemType;
import io.github.dantetam.world.entity.Person;
import io.github.dantetam.world.entity.PersonType;
import io.github.dantetam.world.entity.TechTree;
import io.github.dantetam.world.entity.Tile;
import io.github.dantetam.world.entity.World;

/**
 * Created by Dante on 6/17/2016.
 * A connection between the classes of the world package that store world data,
 * and the OpenGL classes that render the world.
 *
 * This class primarily handles generation of VBOs that are sent to the OpenGL rendering pipeline.
 */
public class WorldHandler {

    public World world;
    public WorldGenerator worldGenerator;

    private MousePicker mousePicker;
    private AssetHelper assetHelper;
    private ChunkHelper chunkHelper;

    private ListModel tilesStored = null;
    //This block is to be rendered
    //public HashMap<Tile.Biome, Solid> storedBiomeTiles; //Store all the hexes grouped by biomes, this way each biome can be rendered with its own texture.
    public HashMap<Integer, Solid> storedTerrainBiomeTiles;
    public HashMap<BuildingType, Solid> storedImprTilesTex;
    public TerrainTextureHelper terrainTextureHelper;

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
    public MapModel<Condition> tileResourceStored;

    public MapModel<Condition> tileUnitUI;

    public RenderEntity highlightedCityTerritory;

    public MapModel<ItemType> improvementResourceProductionUi;
    public MapModel<Condition> improvementResourceStatUi;

    public boolean needsUpdateOnNextFrame = false;

    //public HashMap<Tile, Polygon> hexesShape; //Originally intended to be used for mouse picking. More efficient to use center vertices.

    //private HashMap<BuildingType, Integer> improvementModels;

    private LessonSevenActivity mActivity;
    private OpenGLRenderer mRenderer;

    static final int POSITION_DATA_SIZE = 3;
    static final int NORMAL_DATA_SIZE = 3;
    static final int TEXTURE_COORDINATE_DATA_SIZE = 2;
    static final int BYTES_PER_FLOAT = 4;

    public Collection<ChunkHelper.Node> chunkNodes;
    public int oldIdSum = 0;
    public List<Tile> chunkTiles;
    public boolean chunksUpdated = false;

    //TODO: Use change listeners to update 3d models when code is called

    public WorldHandler(LessonSevenActivity mActivity, OpenGLRenderer mRenderer, MousePicker mousePicker, AssetHelper assetHelper, ChunkHelper chunkHelper, WorldParams params) {
        world = new World(params);
        worldGenerator = new WorldGenerator(mActivity, world, params);
        worldGenerator.init();
        this.mActivity = mActivity;
        this.mRenderer = mRenderer;
        this.mousePicker = mousePicker;
        this.assetHelper = assetHelper;
        this.chunkHelper = chunkHelper;

        //storedBiomeTiles = new HashMap<>();
        storedTerrainBiomeTiles = new HashMap<>();
        storedTileVertexPositions = new HashMap<>();
        storedTileImprovements = new HashMap<>();
        storedTileUnits = new HashMap<>();
    }

    /*public void updateCombatWorld(boolean combatMode) {
        worldRepNeedsUpdate = true;
        if (combatMode) {
            if (world.combatWorld != null) {
                throw new IllegalArgumentException("Attempting to create combat world, but did not pause old one");
            }
            world.combatWorld = new CombatWorld(world, mousePicker.getSelectedTile(), 4);
        }
        else {
            if (world.combatWorld == null) {
                throw new IllegalArgumentException("Attempting to pause combat world that has not been created");
            }
            world.combatWorld.pauseCombatWorld();
            world.combatWorld = null;
        }
    }*/

    public Object[] totalWorldRepresentation() {
        List<BaseModel> modelsToRender = new ArrayList<>();
        List<RenderEntity> solidsToRender = new ArrayList<>();

        Collection<ChunkHelper.Node> nodes = chunkHelper.getChunkNodesContainingTile(mousePicker.centerTile, 1);
        chunksUpdated = false;
        if (chunkNodes == null) {
            chunkNodes = nodes;
            chunksUpdated = true;
        }
        else {
            if (nodes.size() != chunkNodes.size()) {
                chunksUpdated = true;
                chunkNodes = nodes;
            }
            else {
                int idSum = 0;
                for (ChunkHelper.Node node: nodes) {
                    idSum += node.id;
                }
                int temp = oldIdSum;
                oldIdSum = idSum;
                idSum -= temp;
                if (idSum != 0) {
                    chunksUpdated = true;
                }
            }
        }

        if (chunkTiles == null) {
            chunkTiles = new ArrayList<>();
        }
        if (chunksUpdated) {
            System.out.println("updated");
            chunkTiles.clear();
            for (ChunkHelper.Node node: nodes) {
                for (Tile tile: node.tiles) {
                    chunkTiles.add(tile);
                }
            }
        }

        mousePicker.passInTileVertices(storedTileVertexPositions);

        if (needsUpdateOnNextFrame) {
            needsUpdateOnNextFrame = false;
            improvementResourceStatUi = null;
            improvementResourceProductionUi = null;
            highlightedCityTerritory = null;
        }

        if (mRenderer.getCombatMode()) {
            modelsToRender.add(worldRep(world.getAllValidTiles()));
            modelsToRender.add(updateTileUnits());
            modelsToRender.add(tileImprovementRep());

            solidsToRender.add(selectedMarkerRep(ColorTextureHelper.loadColor(255, 255, 255, 255)));
            solidsToRender.add(selectedUnitMarkerRep(ColorTextureHelper.loadColor(255, 255, 255, 255)));

            modelsToRender.add(tileTerritoryRep());

            modelsToRender.add(updateTileUnitUI());
        }
        else {
            modelsToRender.add(worldRep(world.getAllValidTiles()));
            modelsToRender.add(updateTileUnits());
            modelsToRender.add(tileImprovementRep());
            solidsToRender.add(selectedMarkerRep(ColorTextureHelper.loadColor(255, 255, 255, 255)));
            solidsToRender.add(selectedUnitMarkerRep(ColorTextureHelper.loadColor(255, 255, 255, 255)));

            if (storedSelectedTileSolid != null) {
                storedSelectedTileSolid.rotate((float) Math.sin((float) mRenderer.frames / 10f), 0, 1, 0);
            }

            modelsToRender.add(tileTerritoryRep());

            modelsToRender.add(updateTileResourceRep());

            modelsToRender.add(updateTileUnitUI());

            if (previousYieldRep == null) {
                updateTileYieldRep();
            }

            //TODO: Convert to IBOs next?

            if (mousePicker.getSelectedTile() != null) {
                Building impr = mousePicker.getSelectedTile().improvement;
                if (impr != null) {
                    if (impr.buildingType.name.equals("City")) {
                        modelsToRender.add(updateTileYieldRep());
                        modelsToRender.add(tileYieldInterface());
                        if (highlightedCityTerritory == null) {
                            highlightedCityTerritory = createCityTerritoryRep((City) impr);
                            //System.out.println("yes");
                        }
                        if (highlightedCityTerritory != null) {
                            solidsToRender.add(highlightedCityTerritory);
                        }
                    } else {
                        highlightedCityTerritory = null;

                        if (improvementResourceProductionUi == null) {
                            createImprovementResourceRep();
                            modelsToRender.add(improvementResourceProductionUi);
                            modelsToRender.add(improvementResourceStatUi);
                        }
                    }
                }
            } else {
                if (highlightedCityTerritory != null) {
                    highlightedCityTerritory = null;
                }
            }
        }

        /*if (highlightedCityTerritory != null) {
            solidsToRender.add(highlightedCityTerritory);
        }*/

        if (!mRenderer.buildingWorldFinished) {
            mRenderer.buildingWorldFinished = true;
            mActivity.runOnUiThread(new Thread() {
                public void run() {
                    Animation anim = AnimationUtils.loadAnimation(mActivity, R.anim.splash_alpha);
                    anim.reset();
                    ImageView splashScreen = (ImageView) mActivity.findViewById(R.id.splash_screen_main);
                    splashScreen.clearAnimation();
                    splashScreen.startAnimation(anim);

                    mActivity.findViewById(R.id.new_world_options_menu).setVisibility(View.INVISIBLE);
                    mActivity.findViewById(R.id.game_options_menu).setVisibility(View.INVISIBLE);
                    mActivity.findViewById(R.id.new_world_options_menu_scroll).setVisibility(View.INVISIBLE);
                    mActivity.findViewById(R.id.game_options_menu_scroll).setVisibility(View.INVISIBLE);
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

        //tileHighlightRep();
        //modelsToRender.add(tileHighlightOwnerStored);

        return new Object[]{modelsToRender, solidsToRender};
    }

    /**
     * This generates a new VBO for the world as its concrete representation if necessary,
     * and returns it. The idea is that a new VBO should not be generated every time.
     * TODO: Link tiles to positions? So that it is easy to add and remove model VBOs at certain tiles.
     * @return The new VBO.
     */
    private boolean worldRepNeedsUpdate = false;
    public ListModel worldRep(Collection<Tile> tiles) {
        if (tilesStored == null || worldRepNeedsUpdate) {
            tilesStored = new ListModel();

            storedImprTilesTex = new HashMap<>();
            storedTerrainBiomeTiles = new HashMap<>();

            //storedTerrainBiomeTiles = new HashMap<>();

            worldRepNeedsUpdate = false;
            //hexesShape = new HashMap<>();
            //tilesStored.add(generateHexes(world));

            terrainTextureHelper = new TerrainTextureHelper(world);
            //HashMap<Tile.Biome, Integer> biomeTextures = terrainTextureHelper.getBiomeTextures();

            float[][][] solidsOfBiomeData = new float[Tile.Terrain.numTerrains * Tile.Biome.numBiomes][][];

            //float minX = -9999, maxX = -9999, minZ = -9999, maxZ = -9999;
            //float extra = (world.arrayLengthX + 1) % 2 == 1 ? TRANSLATE_FACTORZ * -0.5f : 0;

            int condIndex = 0;
            for (int i = 0; i < Tile.Terrain.numTerrains; i++) {
                for (int j = 0; j < Tile.Biome.numBiomes; j++) {
                    Condition cond = new Condition() {
                        public int desiredType = 0, desiredType2;

                        public void init(int i, int j) {
                            desiredType = i;
                            desiredType2 = j;
                        }

                        public boolean allowed(Object obj) {
                            if (!(obj instanceof Tile)) return false;
                            Tile t = (Tile) obj;
                            //if (t.equals(mousePicker.selectedTile)) return false;
                            return t.terrain.type == desiredType && t.biome.type == desiredType2 && t.improvement == null;
                        }
                    };
                    cond.init(i, j);

                    float[][] solidsOfBiome = generateHexes(world, tiles, cond);

                    solidsOfBiomeData[condIndex] = solidsOfBiome;
                    //tilesStored.add(solidsOfBiome[0]);
                    //tilesStored.add(solidsOfBiome[1]);
                    condIndex++;
                }
            }

            //Find the bounds of the solid being generated. Then pretend that the bounding box is the canvas.
            //We want to 'stretch' the texture generated over this canvas, such that it is seamless.
            //If a and b are the bounding points in one dimension, and x is the in between point, then
            //the percentage of 'betweenness' is measured by (x-a) / (b-a).
            Vector3f maxBounds = new Vector3f((world.arrayLengthX + 1) * TRANSLATE_FACTORX, 0, 1 * TRANSLATE_FACTORZ);
            Vector3f minBounds = new Vector3f(-1 * TRANSLATE_FACTORX, 0, (- world.arrayLengthZ - 1) * TRANSLATE_FACTORZ);
            for (int i = 0; i < solidsOfBiomeData.length; i++) {
                float[][] solidsOfBiome = solidsOfBiomeData[i];
                for (int p = 0; p < solidsOfBiome[0].length / 3; p++) {
                    //Vector3f vertex = new Vector3f(solidsOfBiome[0][p], solidsOfBiome[0][p+1], solidsOfBiome[0][p+2]);
                    float relativeX = (solidsOfBiome[0][3*p] - minBounds.x) / (maxBounds.x - minBounds.x);
                    float relativeZ = (solidsOfBiome[0][3*p + 2] - minBounds.z) / (maxBounds.z - minBounds.z);
                    solidsOfBiome[2][2*p] = relativeX * 3f; solidsOfBiome[2][2*p + 1] = relativeZ * 6.5f;
                }
            }

            condIndex = 0;
            for (int i = 0; i < Tile.Terrain.numTerrains; i++) {
                for (int j = 0; j < Tile.Biome.numBiomes; j++) {
                    Tile.Terrain terrain = Tile.Terrain.fromInt(i);
                    Tile.Biome biome = Tile.Biome.fromInt(j);

                    List<Texture> texture = MultiTextureHelper.getTerrainBiomeTexture(terrain, biome);
                    Solid solid = ObjLoader.loadSolid(texture.get((int) (Math.random() * texture.size())), "worldBiomeTiles" + condIndex, solidsOfBiomeData[condIndex]);
                    storedTerrainBiomeTiles.put(i * Tile.Biome.numBiomes + j, solid);
                    tilesStored.add(solid);

                    condIndex++;
                }
            }

            float[][][] imprData = new float[TechTree.buildingTypes.keySet().size()][][];

            int index = 0;
            for (BuildingType type: TechTree.buildingTypes.values()) {
                Condition cond = new Condition() {
                    public BuildingType desiredType;

                    public void init(Object i) {
                        desiredType = (BuildingType)i;
                    }

                    public boolean allowed(Object obj) {
                        if (!(obj instanceof Tile)) return false;
                        Tile t = (Tile) obj;
                        //if (t.equals(mousePicker.selectedTile)) return false;
                        return t.improvement != null && t.improvement.buildingType.equals(desiredType);
                    }
                };
                cond.init(type);

                float[][] solidsOfBiome = generateHexes(world, tiles, cond);
                imprData[index] = solidsOfBiome;

                index++;
            }

            int i = 0;
            for (BuildingType buildingType: TechTree.buildingTypes.values()) {
                int blackTexture = TextureHelper.loadTexture(mActivity.getResources().getResourceEntryName(R.drawable.dryforest_texture), mActivity, R.drawable.dryforest_texture);
                int rTexture = TextureHelper.loadTexture(mActivity.getResources().getResourceEntryName(R.drawable.desert_texture), mActivity, R.drawable.desert_texture);
                int gTexture = TextureHelper.loadTexture(mActivity.getResources().getResourceEntryName(R.drawable.forest_texture), mActivity, R.drawable.forest_texture);
                int bTexture = TextureHelper.loadTexture(mActivity.getResources().getResourceEntryName(R.drawable.ice_texture), mActivity, R.drawable.ice_texture);
                int blendMap = TextureHelper.loadTexture(mActivity.getResources().getResourceEntryName(R.drawable.usb_android), mActivity, R.drawable.usb_android);

                MultiTexture multiTexture = new MultiTexture("imprHandleMulti" + i, blackTexture, rTexture, gTexture, bTexture, blendMap);
                Solid solid = ObjLoader.loadSolid(multiTexture, "imprBiomeTiles", imprData[i]);
                storedImprTilesTex.put(buildingType, solid);
                tilesStored.add(solid);
                i++;
            }
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
        //HashMap<Clan, List<Tile>> influencers = (HashMap<Clan, List<Tile>>) data[1];
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
        if (city == null) {
            return null;
        }

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

    public RenderEntity createTerrainNeighborRep(City city) {
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
    }

    private void createHighlightRep(MapModel mapOwner, MapModel mapInfluence, List<Tile> tiles) {
        Object[] data = world.aggregateOwners(tiles);
        HashMap<Clan, List<Tile>> owners = (HashMap<Clan, List<Tile>>) data[0];
        HashMap<Clan, List<Tile>> influencers = (HashMap<Clan, List<Tile>>) data[1];

        float[][] objData = ObjLoader.loadObjModelByVertex(mActivity, R.raw.hexagonflat);

        for (Map.Entry<Clan, List<Tile>> en: owners.entrySet()) {
            List<Tile> ownerTiles = en.getValue();
            Clan clan = en.getKey();
            Vector4f drawColor = clan.color;
            int textureHandle = ColorTextureHelper.loadColor(drawColor);
            Solid territoryOwn = tesselSolidsOverTiles(objData, textureHandle, ownerTiles, new Vector3f(0.2f, 0.2f, 0.2f), new Vector3f(0, 0.1f, 0));
            mapOwner.put(clan, territoryOwn);
        }
        for (Map.Entry<Clan, List<Tile>> en: influencers.entrySet()) {
            List<Tile> influenceTiles = en.getValue();
            Clan clan = en.getKey();
            Vector4f drawColor = clan.reducedColor;
            int textureHandle = ColorTextureHelper.loadColor(drawColor);
            Solid territoryInfluence = tesselSolidsOverTiles(objData, textureHandle, influenceTiles, new Vector3f(0.6f, 0.6f, 0.6f), new Vector3f(0, 0.2f, 0));
            mapInfluence.put(clan, territoryInfluence);
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
                    if (tile.improvement != null) {
                        if (previousImprovements.get(tile) == null || !previousImprovements.get(tile).equals(tile.improvement)) {
                            tilesToUpdate.add(tile);
                            previousImprovements.put(tile, tile.improvement);
                        }
                    } else {
                        if (previousImprovements.get(tile) != null && !previousImprovements.get(tile).equals(tile.improvement)) {
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
        if (improvementsStored == null) {
            improvementsStored = new MapModel<>();
        }
        for (Tile tile : tiles) {
            if (tile != null && tile.improvement != null) {
                //Solid improvement = ObjLoader.loadSolid(R.drawable.usb_android, tile.improvement.buildingType.name, assetManager.open(tile.improvement.name + ".obj"));
                //float[][] objData = assetHelper.loadVertexFromAssets(tile.improvement.name + ".obj");
                int modelIndex = tile.hashCode() % tile.improvement.buildingType.modelName.length;
                float[][] objData = assetHelper.loadVertexFromAssets(tile.improvement.buildingType.modelName[modelIndex] + ".obj");
                Solid improvement = tesselSolidsOverTile(objData, mRenderer.mWhiteTextureHandle, tile,
                        new Vector3f(0.06f,0.06f,0.06f), new Vector3f(0,0,0));
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
            {1, -2},
            {1, -1},
            {1, 0},
            {1, 1},
            {1, 2}
    };
    public void createImprovementResourceRep() {
        if (improvementResourceStatUi == null) {
            improvementResourceStatUi = new MapModel<>();
            List<Condition> inputConditions = new ArrayList<>();
            List<Condition> outputConditions = new ArrayList<>();
            Collection<ItemType> items = TechTree.itemTypes.values();
            for (ItemType itemType: items) {
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
                inputCond.init(itemType);
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
                outputCond.init(itemType);
                outputConditions.add(outputCond);
            }

            float[][] hexData = ObjLoader.loadObjModelByVertex("quad", mActivity, R.raw.quad);

            HashMap<ItemType, Integer> textureHandles = new HashMap<>();
            for (ItemType itemType: items) {
                int resId = mActivity.getResources().getIdentifier(itemType.name, "drawable", mActivity.getPackageName());
                textureHandles.put(itemType, TextureHelper.loadTexture(itemType.iconName, mActivity, resId));
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

                float[] offset = imprIconInputOffsets[0];
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

            int i = 0;
            for (ItemType itemType: items) {
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
                Solid hexes = ObjLoader.loadSolid(textureHandles.get(itemType), null, generatedData);
                hexes.alphaEnabled = true;

                improvementResourceStatUi.put(cond, hexes);
            }
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
                previousTileFood.put(tile, tile.food());
                previousTileProduction.put(tile, tile.production());
                previousTileScience.put(tile, tile.science());
                previousTileGold.put(tile, tile.capital());
            }
            updateTileYieldRep(world.getAllValidTiles());
        }
        else {
            List<Tile> tilesToUpdate = new ArrayList<>();
            for (Tile tile: world.getAllValidTiles()) {
                if (previousTileFood.get(tile) != tile.food() ||
                        previousTileProduction.get(tile) != tile.production() ||
                        previousTileScience.get(tile) != tile.science() ||
                        previousTileGold.get(tile) != tile.capital()) {
                    tilesToUpdate.add(tile);
                    previousTileFood.put(tile, tile.food());
                    previousTileProduction.put(tile, tile.production());
                    previousTileScience.put(tile, tile.science());
                    previousTileGold.put(tile, tile.capital());
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
                return t.food() > 0;
            }
        };
        Condition condition2 = new Condition() {
            public boolean allowedTile(Tile t) {
                return t.production() > 0;
            }
        };
        Condition condition3 = new Condition() {
            public boolean allowedTile(Tile t) {
                return t.science() > 0;
            }
        };
        Condition condition4 = new Condition() {
            public boolean allowedTile(Tile t) {
                return t.capital() > 0;
            }
        };
        Condition condition5 = new Condition() {
            public boolean allowedTile(Tile t) {
                return t.improvement != null && t.improvement.buildingType.name.equals("City") && ((City) t.improvement).population() > 0;
            }
        };
        Condition condition6 = new Condition() {
            public boolean allowedTile(Tile t) {
                return t.improvement != null;
            }
        };

        Condition[] conditions = {condition1, condition2, condition3, condition4, condition5, condition6};

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
        if (tileResourceStored == null) {
            tileResourceStored = new MapModel<>();
            Collection<ItemType> items = TechTree.itemTypes.values();
            HashMap<ItemType, Integer> resourceTextureHandles = new HashMap<>();
            List<Condition> resourceConditions = new ArrayList<>();
            for (ItemType itemType: items) {
                String resourceName = itemType.getAndroidResourceName();

                int resId = mActivity.getResources().getIdentifier(resourceName, "drawable", mActivity.getPackageName());
                if (resId == 0) {
                    System.out.println("Could not find " + resourceName + " in drawable, using usb_android");
                    resourceTextureHandles.put(itemType, TextureHelper.loadTexture("usb_android", mActivity, R.drawable.usb_android));
                }
                else
                    resourceTextureHandles.put(itemType, TextureHelper.loadTexture(resourceName, mActivity, resId));

                Condition cond = new Condition() {
                    public ItemType type;

                    public boolean allowedTile(Tile t) {
                        return t.resources.size() > 0 && t.resources.get(0).type.equals(type);
                    }

                    public void init(Object obj) {
                        type = (ItemType) obj;
                    }
                };
                cond.init(itemType);
                resourceConditions.add(cond);
            }

            int i = 0;
            for (ItemType itemType: items) {
                Condition cond = resourceConditions.get(i);

                List<Tile> tilesToRender = new ArrayList<>();
                for (Tile tile : world.getAllValidTiles()) {
                    if (cond.allowedTile(tile)) {
                        tilesToRender.add(tile);
                    }
                }

                float[][] hexData = ObjLoader.loadObjModelByVertex(mActivity, R.raw.quad);
                int textureHandle = resourceTextureHandles.get(itemType);
                Solid hexes = tesselSolidsOverTiles(hexData, textureHandle, tilesToRender,
                        new Vector3f(0.66f,1f,0.66f),
                        new Vector3f(1*TRANSLATE_FACTOR_UI_X,0.3f,1*TRANSLATE_FACTOR_UI_Z));
                tileResourceStored.put(cond, hexes);
                i++;
            }
        }
        return tileResourceStored;
    }


    public MapModel<Condition> updateTileUnitUI() {
        if (tileUnitUI == null) {
            tileUnitUI = new MapModel<>();
            Collection<PersonType> items = TechTree.personTypes.values();
            HashMap<PersonType, Integer> resourceTextureHandles = new HashMap<>();
            List<Condition> resourceConditions = new ArrayList<>();
            for (PersonType personType: items) {
                String resourceName = personType.getAndroidResourceName();

                int resId = mActivity.getResources().getIdentifier(resourceName, "drawable", mActivity.getPackageName());
                if (resId == 0) {
                    System.out.println("Could not find " + resourceName + " in drawable, using usb_android");
                    resourceTextureHandles.put(personType, TextureHelper.loadTexture("usb_android", mActivity, R.drawable.usb_android));
                }
                else
                    resourceTextureHandles.put(personType, TextureHelper.loadTexture(resourceName, mActivity, resId));

                Condition cond = new Condition() {
                    public PersonType type;

                    public boolean allowedTile(Tile t) {
                        return t.occupants.size() > 0 && t.occupants.get(0) instanceof Person && ((Person) t.occupants.get(0)).personType.equals(type);
                    }

                    public void init(Object obj) {
                        type = (PersonType) obj;
                    }
                };
                cond.init(personType);
                resourceConditions.add(cond);
            }

            int i = 0;
            for (PersonType personType: items) {
                Condition cond = resourceConditions.get(i);

                List<Tile> tilesToRender = new ArrayList<>();
                for (Tile tile : world.getAllValidTiles()) {
                    if (cond.allowedTile(tile)) {
                        tilesToRender.add(tile);
                    }
                }

                float[][] hexData = ObjLoader.loadObjModelByVertex(mActivity, R.raw.quad);
                int textureHandle = resourceTextureHandles.get(personType);
                Solid hexes = tesselSolidsOverTiles(hexData, textureHandle, tilesToRender,
                        new Vector3f(0.66f,1f,0.66f), new Vector3f(0,3,0));

                //hexes.alphaEnabled = true;
                tileUnitUI.put(cond, hexes);
                i++;
            }
        }
        return tileUnitUI;
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
                        return tile.food() == num;
                    } else if (type == 1) {
                        return tile.production() == num;
                    } else if (type == 2) {
                        return tile.science() == num;
                    } else if (type == 3) {
                        return tile.capital() == num;
                    } else if (type == 4) {
                        return tile.improvement != null && tile.improvement.buildingType.name.equals("City") && ((City) tile.improvement).population() == num;
                    } else {
                        throw new IllegalArgumentException("Invalid type in variable condition");
                    }
                }
            };

            float[][] hexData = ObjLoader.loadObjModelByVertex(mActivity, R.raw.quad);
            for (int i = 0; i <= 4; i++) {
                float[] offset = numOffsets[i];
                Vector3f trueOffset = new Vector3f(offset[0]*TRANSLATE_FACTOR_UI_X, 0.6f, offset[1]*TRANSLATE_FACTOR_UI_Z);
                for (int j = 1; j <= 9; j++) {
                    variableCond.init(i, j);
                    List<Tile> tilesToRender = new ArrayList<>();
                    for (Tile tile: world.getAllValidTiles()) {
                        if (variableCond.allowedTile(tile)) {
                            tilesToRender.add(tile);
                        }
                    }
                    int textureHandle = TextureHelper.loadTexture("ui_" + j, mActivity, yieldUiStatMarkers[j]);
                    Solid hexes = tesselSolidsOverTiles(hexData, textureHandle, tilesToRender,
                            new Vector3f(0.66f,1f,0.66f), trueOffset);
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
        int testTexture = TextureHelper.loadTexture("usb_android", mActivity, R.drawable.usb_android);
        if (unitsStored == null) {
            unitsStored = new MapModel<>();
        }
        for (Tile tile : tiles) {
            if (tile != null) {
                if (tile.occupants.size() == 0) {
                    //storedTileUnits.put(tile, null);
                }
                else {
                    float[][][] unitsData = new float[tile.occupants.size()][][];
                    int totalLength = 0;
                    for (int i = 0; i < tile.occupants.size(); i++) {
                        Entity en = tile.occupants.get(i);
                        float[][] finalData = new float[3][];
                        if (en instanceof Person) {
                            if (((Person) en).personType.modelName != null && ((Person) en).completionPercentage() >= 1.0) {
                                PersonType personType = ((Person) en).personType;
                                float[][] unitData = assetHelper.loadVertexFromAssets(personType.modelName + ".obj");
                                float[] scaledData = scaleData(unitData[0], personType.modelScale, personType.modelScale, personType.modelScale);
                                finalData[0] = scaledData;
                                finalData[1] = unitData[1]; finalData[2] = unitData[2];
                            }
                            else {
                                System.err.println("No designated model for " + en.name + ", using default basic_box_char.obj");
                                finalData = assetHelper.loadVertexFromAssets("basic_box_char.obj");
                            }
                        }
                        else {
                            finalData = assetHelper.loadVertexFromAssets(en.name + ".obj");
                        }
                        unitsData[i] = finalData;
                        totalLength += finalData[0].length;
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
                        Solid improvement = ObjLoader.loadSolid(testTexture, null, improvementData);

                        unitsStored.put(en, improvement);
                        //storedTileUnits.put(en, improvement);
                    }
                }
            }
        }
        return unitsStored;
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
            storedSelectedTileSolid = tesselSolidsOverTile(hexData, textureHandle, selected,
                    new Vector3f(1,0,1), new Vector3f(0, 0.06f, 0));
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
            float[][] hexData = ObjLoader.loadObjModelByVertex(mActivity, R.raw.hexagon);
            storedSelectedUnitSolid = tesselSolidsOverTile(hexData, textureHandle, selectedLocation,
                    new Vector3f(1,0,1), new Vector3f(0, 0.1f, 0));
        }
        return storedSelectedUnitSolid;
    }

    /**
     *
     * @param //textureHandle The texture for which this VBO will have
     * @param world The world to represent
     * @param condition Some sort of restriction, if necessary. In this case,
     *                  we check that the tiles are of a certain biome.
     *                  This is so that we render all tiles of the biome,
     *                  under the same texture and VBO.
     * @return New interleaved buffer which is a representation of the world's tiles within condition
     *
     * This method returns float data as opposed to a solid because individual biomes need to be textured as a group.
     */
    private static final float TRANSLATE_FACTORX = 3.3f;
    private static final float TRANSLATE_FACTORZ = 4f;
    private float[][] generateHexes(World world, Collection<Tile> tiles, Condition condition) {
        //Load the vtn data of one hex obj
        //float[][] newHexData = ObjLoader.loadObjModelByVertex(mActivity, R.raw.hexagon);
        float[][] oldHexData = ObjLoader.loadObjModelByVertex(mActivity, R.raw.newflathexagon);

        //int mRequestedCubeFactor = WORLD_LENGTH;

        //Count the number of hexes needed so that the correct space is allocated
        int numHexesToRender = tiles.size();

        //Create some appropriately sized tables which will store preliminary buffer data
        //Combine them all within these pieces of data.
        final float[] totalCubePositionData = new float[oldHexData[0].length * numHexesToRender];
        int cubePositionDataOffset = 0;
        final float[] totalNormalPositionData = new float[oldHexData[0].length / POSITION_DATA_SIZE * NORMAL_DATA_SIZE * numHexesToRender];
        int cubeNormalDataOffset = 0;
        final float[] totalTexturePositionData = new float[oldHexData[0].length / POSITION_DATA_SIZE * TEXTURE_COORDINATE_DATA_SIZE * numHexesToRender];
        int cubeTextureDataOffset = 0;

        int xx = 0, zz = 0;
        for (int x = 0; x < world.arrayLengthX; x++) {
            for (int z = 0; z < world.arrayLengthZ; z++) {
                Tile tile = world.getTile(x,z);
                if (tile == null) continue;
                zz++;

                //Scale and translate accordingly so everything fits together
                float extra = x % 2 == 1 ? TRANSLATE_FACTORZ * -0.5f : 0;

                //Store these positions for later use when we place tile improvements and such
                Vector3f vertices = new Vector3f(xx * TRANSLATE_FACTORX, tile.elevation / 16f, - zz * TRANSLATE_FACTORZ + extra);
                storedTileVertexPositions.put(tile, vertices);

                if (condition.allowed(tile) && tiles.contains(tile)) {
                    //tile.elevation = 0;

                    final float[] scaledData = scaleData(oldHexData[0], 1, 0, 1);

                    final float[] thisCubePositionData = translateData(scaledData, vertices.x, vertices.y, vertices.z);

                    //Interleave all the new vtn data, per hex.
                    System.arraycopy(thisCubePositionData, 0, totalCubePositionData, cubePositionDataOffset, thisCubePositionData.length);
                    cubePositionDataOffset += thisCubePositionData.length;

                    System.arraycopy(oldHexData[1], 0, totalNormalPositionData, cubeNormalDataOffset, oldHexData[1].length);
                    cubeNormalDataOffset += oldHexData[1].length;
                    System.arraycopy(oldHexData[2], 0, totalTexturePositionData, cubeTextureDataOffset, oldHexData[2].length);
                    cubeTextureDataOffset += oldHexData[2].length;
                }
            }
            xx++;
            zz = 0;
        }

        //tesselatedHexes = new float[][]{totalCubePositionData, totalNormalPositionData, totalTexturePositionData};
        //Solid hexes = ObjLoader.loadSolid(textureHandle, null, tesselatedHexes);
        return new float[][]{totalCubePositionData, totalNormalPositionData, totalTexturePositionData};
    }

    /**
     * In these rendering methods, it is more efficient to 'tessellate' obj files into a single interleaved buffer
     * i.e. copy the vertex data over and over again into a single buffer containing a list of possibly unconnected
     * vertices.
     *
     * This is a utility method which copies the data over tiles, which are a translation.
     * @param objData Contains three float[], which are vertex, normal, and texture data
     * @param textureHandle ID of the texture to be used
     * @param tilesToRender Repeat the obj over these tiles
     * @param scale 3D scale transformation of every individual OBJ
     * @param trueOffset A 3D vector for extra custom translation along these axes
     */
    private Solid tesselSolidsOverTiles(float[][] objData, int textureHandle, List<Tile> tilesToRender, Vector3f scale,
                                        Vector3f trueOffset) {
        int numHexesToRender = tilesToRender.size();
        final float[] totalCubePositionData = new float[objData[0].length * numHexesToRender];
        int cubePositionDataOffset = 0;
        final float[] totalNormalPositionData = new float[objData[0].length / POSITION_DATA_SIZE * NORMAL_DATA_SIZE * numHexesToRender];
        int cubeNormalDataOffset = 0;
        final float[] totalTexturePositionData = new float[objData[0].length / POSITION_DATA_SIZE * TEXTURE_COORDINATE_DATA_SIZE * numHexesToRender];
        int cubeTextureDataOffset = 0;

        for (Tile tile : tilesToRender) {
            Vector3f vertices = storedTileVertexPositions.get(tile);
            float[] scaledData = scaleData(objData[0], scale);
            final float[] thisCubePositionData = translateData(scaledData,
                    vertices.x + trueOffset.x, vertices.y + trueOffset.y, vertices.z + trueOffset.z);

            //Interleave all the new vtn data, per hex.
            System.arraycopy(thisCubePositionData, 0, totalCubePositionData, cubePositionDataOffset, thisCubePositionData.length);
            cubePositionDataOffset += thisCubePositionData.length;
            System.arraycopy(objData[1], 0, totalNormalPositionData, cubeNormalDataOffset, objData[1].length);
            cubeNormalDataOffset += objData[1].length;
            System.arraycopy(objData[2], 0, totalTexturePositionData, cubeTextureDataOffset, objData[2].length);
            cubeTextureDataOffset += objData[2].length;
        }

        float[][] generatedData = new float[][]{totalCubePositionData, totalNormalPositionData, totalTexturePositionData};
        Solid tessellatedObj = ObjLoader.loadSolid(textureHandle, null, generatedData);
        return tessellatedObj;
    }
    private Solid tesselSolidsOverTile(float[][] objData, int textureHandle, Tile renderTile, Vector3f scale, Vector3f trueOffset) {
        return tesselSolidsOverTiles(objData, textureHandle, Arrays.asList(renderTile), scale, trueOffset);
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
     * @param scaleVec A vector to scale by
     * @return A new set of data where each x point (0, 3, 6...) is scaled by dx,
     * y (1, 4, 7...) by dy, and z (2, 5, 8...) by dz.
     */
    private float[] scaleData(float[] data, Vector3f scaleVec) {
        float[] newData = new float[data.length];
        for (int i = 0; i < data.length; i++) {
            if (i % 3 == 0) newData[i] = data[i] * scaleVec.x;
            else if (i % 3 == 1) newData[i] = data[i] * scaleVec.y;
            else newData[i] = data[i] * scaleVec.z;
        }
        return newData;
    }
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
