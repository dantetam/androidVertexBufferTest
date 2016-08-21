package io.github.dantetam.opstrykontest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.util.HashMap;

import io.github.dantetam.world.Tile;
import io.github.dantetam.world.World;

/**
 * Created by Dante on 8/14/2016.
 */
public class TerrainTextureHelper {

    private World world;

    private HashMap<Tile.Biome, Integer> biomeTextures;
    //public int TEXTURE_SIZE_PIXELS = 256;

    public TerrainTextureHelper(World w) {
        world = w;
        //biomeTextures = new HashMap<>();
    }

    public HashMap<Tile.Biome, Integer> getBiomeTextures() {
        if (biomeTextures == null) {
            biomeTextures = new HashMap<>();
            Tile.Biome[] biomes = Tile.Biome.values();
            for (Tile.Biome biome: biomes) {
                Bitmap bitmap = generateNoiseBitmap(biome);
                int textureHandle = TextureHelper.loadTexture("biomeTexture" + biome.toString(), bitmap);
                biomeTextures.put(biome, textureHandle);
            }
        }
        return biomeTextures;
    }

    //Generate a new one or get one that's already been created?
    private Bitmap generateNoiseBitmap(Tile.Biome biome) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;	// No pre-scaling
        options.inMutable = true;
        //final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.usb_android, options);
        Bitmap result = BitmapHelper.findBitmapOrBuild(R.drawable.noise4);
        Bitmap noise = BitmapHelper.findBitmapOrBuild(R.drawable.noise4);

        float[] biomeColor = Tile.Biome.colorFromInt(biome.type);
        int r = (int)biomeColor[0]*255, g = (int)biomeColor[1]*255, b = (int)biomeColor[2]*255, a = (int)biomeColor[3]*255;

        //int gray = (int)(Math.random()*255);
        //int blendR = (r + gray) / 2; int blendG = (g + gray) / 2; int blendB = (b + gray) / 2; int blendA = 255;
        //int rgba = ColorTextureHelper.intFromColor(blendR, blendG, blendB, blendA);

        int textureFactor = noise.getWidth() / result.getWidth();

        for (int y = 0; y < result.getHeight(); y++) {
            for (int x = 0; x < result.getWidth(); x++) {
                float worldPositionX = (int)((float) x / (float) result.getWidth() * (float) world.arrayLengthX);
                float worldPositionZ = (int)((float) y / (float) result.getHeight() * (float) world.arrayLengthZ);

                //int noiseX = x * textureFactor, noiseY = y * textureFactor;

                int noiseColor = noise.getPixel(x * textureFactor, y * textureFactor);
                int nr = (noiseColor >>> 16) & 255;
                int ng = (noiseColor >>> 8) & 255;
                int nb = (noiseColor >>> 0) & 255;
                int na = (noiseColor >>> 24) & 255;

                int rgba;

                Tile respTile = world.getTile((int)worldPositionX, (int)worldPositionZ);
                if (respTile != null) {
                    int terrainNumProportion = (int)((float) Math.abs(Tile.Biome.numBiomes / 2 - respTile.biome.type) / (float) Tile.Biome.numBiomes * 255f) + 128;
                    //nr = (nr + terrainNumProportion) / 2;
                    //ng = (ng + terrainNumProportion) / 2;
                    //nb = (nb + terrainNumProportion) / 2;
                    //rgba = ColorTextureHelper.intFromColor((r + terrainNumProportion) / 2, (g + terrainNumProportion) / 2, (b + terrainNumProportion) / 2, 255);
                    //rgba = ColorTextureHelper.intFromColor((r + nr) / 2, (g + ng) / 2, (b + nb) / 2, 255);
                    rgba = ColorTextureHelper.intFromColor(nr, ng, nb, 255);
                }
                else {
                    //rgba = ColorTextureHelper.intFromColor(255, 255, 255, 255);
                    rgba = ColorTextureHelper.intFromColor(nr, ng, nb, 255);
                }
                //int rgba = ColorTextureHelper.intFromColor((r + nr) / 2, (g + ng) / 2, (b + nb) / 2, 255);
                result.setPixel(x, y, rgba);
            }
        }

        //Bitmap bitMap = Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);
        return result;
    }

    //RGBA + RGBA -> RGBA
    //TODO: Fix this so that it's the correct inverse of photon flux.
    public static int[] blend(int[] start, int[] dest, double frac) {
        double squared = frac*frac; double oneMinus = 1 - squared;
        double r = squared*start[0] + oneMinus*dest[0];
        double g = squared*start[1] + oneMinus*dest[1];
        double b = squared*start[2] + oneMinus*dest[2];
        double a = (start[3] + dest[3]) / 2d;
        return new int[]{(int)r, (int)g, (int)b, (int)a};
    }

}
