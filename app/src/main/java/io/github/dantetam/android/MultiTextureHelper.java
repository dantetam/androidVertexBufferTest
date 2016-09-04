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
import io.github.dantetam.world.entity.Tile;
import io.github.dantetam.xml.BuildingXmlParser;

/**
 * Created by Dante on 9/3/2016.
 */
public class MultiTextureHelper {

    private static LessonSevenActivity mActivity;
    public static HashMap<String, List<Texture>> buildingTextures;
    public static HashMap<Tile.Terrain, List<Texture>> terrainTextures;

    public static boolean init = false;
    public static void init(LessonSevenActivity activity) {
        if (init) {
            return;
        }
        init = true;
        mActivity = activity;
        buildingTextures = new HashMap<>();
        terrainTextures = new HashMap<>();
        initBuildings();
        initTerrain();
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
        buildingTextures.put("Farm", tex);
    }

    public static void initTerrain() {
        int dryforest = load(R.drawable.dryforest_texture);
        int desert = load(R.drawable.desert_texture);
        int forest = load(R.drawable.forest_texture);
        int ice = load(R.drawable.ice_texture);

        List<Texture> tex = new ArrayList<>();
        tex.add(new MultiTexture("hill1", dryforest, desert, forest, ice, load(R.drawable.hill_blendmap)));
        tex.add(new MultiTexture("hill2", dryforest, desert, forest, ice, load(R.drawable.hill_blendmap_2)));
        terrainTextures.put(Tile.Terrain.HILLS, tex);

        List<Texture> tex1 = new ArrayList<>();
        tex1.add(new MultiTexture("island1", load(R.drawable.shallow_sea_texture), dryforest, forest, ice, load(R.drawable.island_blendmap_4)));
        tex1.add(new MultiTexture("island2", load(R.drawable.shallow_sea_texture), dryforest, forest, ice, load(R.drawable.island_blendmap_4)));
        tex1.add(new MultiTexture("island3", load(R.drawable.shallow_sea_texture), dryforest, forest, ice, load(R.drawable.island_blendmap_4)));
        tex1.add(new MultiTexture("island4", load(R.drawable.shallow_sea_texture), dryforest, forest, ice, load(R.drawable.island_blendmap_4)));
        terrainTextures.put(Tile.Terrain.ISLANDS, tex1);

        List<Texture> tex2 = new ArrayList<>();
        tex2.add(new MultiTexture("mountain1", dryforest, desert, forest, ice, load(R.drawable.mountain_blendmap)));
        terrainTextures.put(Tile.Terrain.MOUNTAINS, tex2);

        List<Texture> tex3 = new ArrayList<>();
        tex3.add(new MultiTexture("plains1", forest, forest, forest, ice, load(R.drawable.noise1)));
        tex3.add(new MultiTexture("plains2", forest, forest, forest, ice, load(R.drawable.noise2)));
        terrainTextures.put(Tile.Terrain.PLAINS, tex3);

        List<Texture> tex4 = new ArrayList<>();
        tex4.add(new MultiTexture("cliffs1", desert, desert, desert, desert, load(R.drawable.mountain_blendmap)));
        terrainTextures.put(Tile.Terrain.CLIFFS, tex4);

        List<Texture> tex5 = new ArrayList<>();
        tex5.add(new Texture("shallow_sea1", load(R.drawable.deep_sea_texture)));
        terrainTextures.put(Tile.Terrain.SHALLOW_SEA, tex5);

        List<Texture> tex6 = new ArrayList<>();
        tex6.add(new Texture("deep_sea1", load(R.drawable.deep_sea_texture)));
        terrainTextures.put(Tile.Terrain.DEEP_SEA, tex6);
    }

}
