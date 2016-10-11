package io.github.dantetam.android;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.dantetam.opengl.MultiTexture;
import io.github.dantetam.opengl.Solid;
import io.github.dantetam.opengl.Texture;
import io.github.dantetam.opstrykontest.Condition;
import io.github.dantetam.opstrykontest.LessonSevenActivity;
import io.github.dantetam.opstrykontest.R;
import io.github.dantetam.world.entity.BuildingType;
import io.github.dantetam.world.entity.ItemType;
import io.github.dantetam.world.entity.TechTree;
import io.github.dantetam.world.entity.Tile;
import io.github.dantetam.xml.BuildingXmlParser;

/**
 * Created by Dante on 9/3/2016.
 */
public class MultiTextureHelper {

    private static LessonSevenActivity mActivity;
    public static HashMap<String, List<Texture>> buildingTextures;

    public static HashMap<TerrainBiomePair, List<Texture>> terrainBiomeTextures;

    public static HashMap<ItemType, List<Texture>> resourceTextures;

    public static boolean init = false;
    public static void init(LessonSevenActivity activity) {
        if (init) {
            return;
        }
        init = true;
        mActivity = activity;
        buildingTextures = new HashMap<>();
        terrainBiomeTextures = new HashMap<>();
        resourceTextures = new HashMap<>();
        initBuildings();
        initTerrainAndBiome();
        initResources();
    }

    public static int load(int resourceId) {
        return TextureHelper.loadTexture(mActivity, resourceId);
    }

    public static void initBuildings() {
        int dryforest = TextureHelper.loadTexture(mActivity.getResources().getResourceEntryName(R.drawable.dryforest_texture), mActivity, R.drawable.dryforest_texture);
        int desert = TextureHelper.loadTexture(mActivity.getResources().getResourceEntryName(R.drawable.desert_texture), mActivity, R.drawable.desert_texture);
        int forest = TextureHelper.loadTexture(mActivity.getResources().getResourceEntryName(R.drawable.forest_texture), mActivity, R.drawable.forest_texture);
        int ice = TextureHelper.loadTexture(mActivity.getResources().getResourceEntryName(R.drawable.ice_texture), mActivity, R.drawable.ice_texture);

        int blendMap = TextureHelper.loadTexture(mActivity.getResources().getResourceEntryName(R.drawable.blend_map), mActivity, R.drawable.blend_map);

        List<Texture> tex = new ArrayList<>();
        tex.add(new MultiTexture("farm", dryforest, desert, forest, ice, blendMap));
        buildingTextures.put("farm", tex);
    }

    public static void initTerrainAndBiome() {
        int dryforest = load(R.drawable.dryforest_texture);
        int desert = load(R.drawable.desert_texture);
        int forest = load(R.drawable.forest_texture);
        int rainforest = load(R.drawable.extromass_blendmap);
        int ice = load(R.drawable.ice_texture);

        int shallowSea = load(R.drawable.shallow_sea_texture);
        int deepSea = load(R.drawable.deep_sea_texture);
        int solidSea = load(R.drawable.sea_texture_plain);

        int[] baseColorTextures;

        for (Tile.Biome biome: Tile.Biome.values()) {
            switch (biome) {
                case SEA:
                    baseColorTextures = new int[]{solidSea, solidSea, solidSea, solidSea};
                    break;
                case ICE:
                    baseColorTextures = new int[]{ice, ice, forest, shallowSea};
                    break;
                case TUNDRA:
                    baseColorTextures = new int[]{ice, ice, dryforest, dryforest};
                    break;
                case DESERT:
                    baseColorTextures = new int[]{desert, desert, desert, desert};
                    break;
                case STEPPE:
                    baseColorTextures = new int[]{dryforest, dryforest, dryforest, dryforest};
                    break;
                case FOREST:
                    baseColorTextures = new int[]{forest, forest, forest, forest};
                    break;
                case RAINFOREST:
                    baseColorTextures = new int[]{rainforest, rainforest, rainforest, rainforest};
                    break;
                default:
                    baseColorTextures = new int[]{};
                    break;
            }
            List<Texture> tex = new ArrayList<>();
            tex.add(new MultiTexture("hill1", baseColorTextures[0], baseColorTextures[1], baseColorTextures[2], baseColorTextures[3], load(R.drawable.hill_blendmap)));
            tex.add(new MultiTexture("hill2", baseColorTextures[0], baseColorTextures[1], baseColorTextures[2], baseColorTextures[3], load(R.drawable.hill_blendmap_2)));
            terrainBiomeTextures.put(new TerrainBiomePair(Tile.Terrain.HILLS, biome), tex);

            List<Texture> tex1 = new ArrayList<>();
            tex1.add(new MultiTexture("island1", baseColorTextures[0], baseColorTextures[1], baseColorTextures[2], baseColorTextures[3], load(R.drawable.island_blendmap_2)));
            tex1.add(new MultiTexture("island2", baseColorTextures[0], baseColorTextures[1], baseColorTextures[2], baseColorTextures[3], load(R.drawable.island_blendmap_2)));
            tex1.add(new MultiTexture("island3", baseColorTextures[0], baseColorTextures[1], baseColorTextures[2], baseColorTextures[3], load(R.drawable.island_blendmap_2)));
            tex1.add(new MultiTexture("island4", baseColorTextures[0], baseColorTextures[1], baseColorTextures[2], baseColorTextures[3], load(R.drawable.island_blendmap_2)));
            terrainBiomeTextures.put(new TerrainBiomePair(Tile.Terrain.ISLANDS, biome), tex1);

            List<Texture> tex2 = new ArrayList<>();
            tex2.add(new MultiTexture("mountain1", baseColorTextures[0], baseColorTextures[1], baseColorTextures[2], baseColorTextures[3], load(R.drawable.mountain_blendmap)));
            terrainBiomeTextures.put(new TerrainBiomePair(Tile.Terrain.MOUNTAINS, biome), tex2);

            List<Texture> tex3 = new ArrayList<>();
            tex3.add(new MultiTexture("plains1", baseColorTextures[0], baseColorTextures[1], baseColorTextures[2], baseColorTextures[3], load(R.drawable.noise1)));
            tex3.add(new MultiTexture("plains2", baseColorTextures[0], baseColorTextures[1], baseColorTextures[2], baseColorTextures[3], load(R.drawable.noise2)));
            terrainBiomeTextures.put(new TerrainBiomePair(Tile.Terrain.PLAINS, biome), tex3);

            List<Texture> tex4 = new ArrayList<>();
            tex4.add(new MultiTexture("cliffs1", baseColorTextures[0], baseColorTextures[1], baseColorTextures[2], baseColorTextures[3], load(R.drawable.mountain_blendmap)));
            terrainBiomeTextures.put(new TerrainBiomePair(Tile.Terrain.CLIFFS, biome), tex4);

            List<Texture> tex5 = new ArrayList<>();
            tex5.add(new MultiTexture("shallow_sea1", baseColorTextures[0], baseColorTextures[1], baseColorTextures[2], baseColorTextures[3], load(R.drawable.deep_sea_texture)));
            terrainBiomeTextures.put(new TerrainBiomePair(Tile.Terrain.SHALLOW_SEA, biome), tex5);

            List<Texture> tex6 = new ArrayList<>();
            tex6.add(new MultiTexture("deep_sea1", baseColorTextures[0], baseColorTextures[1], baseColorTextures[2], baseColorTextures[3], load(R.drawable.deep_sea_texture)));
            terrainBiomeTextures.put(new TerrainBiomePair(Tile.Terrain.DEEP_SEA, biome), tex6);
        }
    }

    public static void initResources() {
        int extromass = load(R.drawable.extromass_blendmap);

        List<Texture> tex = new ArrayList<>();
        tex.add(new MultiTexture("extromass", extromass, extromass, extromass, extromass, extromass));
        resourceTextures.put(TechTree.itemTypes.get("Extromass"), tex);
    }

    public static class TerrainBiomePair {
        public Tile.Terrain terrain;
        public Tile.Biome biome;
        public TerrainBiomePair(Tile.Terrain t, Tile.Biome b) {
            terrain = t;
            biome = b;
        }
        public int hashCode() {
            return terrain.type * 17 + biome.type;
        }
        public boolean equals(Object other) {
            if (!(other instanceof TerrainBiomePair)) {
                return false;
            }
            TerrainBiomePair pair = (TerrainBiomePair) other;
            return terrain.equals(pair.terrain) && biome.equals(pair.biome);
        }
    }

    public static List<Texture> getTerrainBiomeTexture(Tile.Terrain terrain, Tile.Biome biome) {
        TerrainBiomePair pair = new TerrainBiomePair(terrain, biome);
        //System.out.println(Tile.Terrain.nameFromInt(terrain.type) + " " + Tile.Biome.nameFromInt(biome.type) + " " + terrainBiomeTextures.get(pair));
        return terrainBiomeTextures.get(pair);
    }

}
