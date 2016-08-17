package io.github.dantetam.opstrykontest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
        Bitmap bitmap = BitmapHelper.findBitmapOrBuild(R.drawable.solid_color);
        Bitmap noise = BitmapHelper.findBitmapOrBuild(R.drawable.noise2);

        float[] biomeColor = Tile.Biome.colorFromInt(biome.type);
        int r = (int)biomeColor[0]*255, g = (int)biomeColor[1]*255, b = (int)biomeColor[2]*255, a = (int)biomeColor[3]*255;

        //int gray = (int)(Math.random()*255);
        //int blendR = (r + gray) / 2; int blendG = (g + gray) / 2; int blendB = (r + gray) / 2; int blendA = 255;
        //int rgba = ColorTextureHelper.intFromColor(blendR, blendG, blendB, blendA);

        for (int y = 0; y < bitmap.getHeight(); y++){
            for (int x = 0; x < bitmap.getWidth(); x++){
                int worldPositionX = (int)((float) x / (float) bitmap.getWidth() * (float) world.arrayLengthX);
                int worldPositionZ = (int)((float) y / (float) bitmap.getHeight() * (float) world.arrayLengthZ);
                Tile respTile = world.getTile(worldPositionX, worldPositionZ);
                if (respTile != null) {

                }
                int noiseColor = noise.getPixel(x, y);
                int nr = (noiseColor >>> 16) & 255;
                int ng = (noiseColor >>> 8) & 255;
                int nb = (noiseColor >>> 0) & 255;
                int na = (noiseColor >>> 24) & 255;

                int rgba = ColorTextureHelper.intFromColor((r + nr) / 2, (g + ng) / 2, (b + nb) / 2, 255);
                bitmap.setPixel(x, y, rgba);
            }
        }
        //Bitmap bitMap = Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);
        return bitmap;
    }

}
