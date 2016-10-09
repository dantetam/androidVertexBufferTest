package io.github.dantetam.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.util.HashMap;

import io.github.dantetam.opstrykontest.R;

/*
A helper class which is solely responsible for loading all textures.
All textures must be loaded here so their names can be stored in the hash map.
 */
public class TextureHelper
{
    //Stores name of texture and its respective OpenGL handle
    public static HashMap<String, Integer> texturesByName = new HashMap<>();
    public static HashMap<String, Drawable> drawablesById = new HashMap<>();

    /*
    Load a texture and look it up solely by name.
     */
    public static int loadTexture(final String name) {
        if (texturesByName.containsKey(name)) {
            return texturesByName.get(name);
        }
        return -1;
    }

    public static HashMap<Integer, String> resourceIdAndNames = new HashMap<>();

    public static int loadTexture(Context context, int resourceId) {
        String name = resourceIdAndNames.get(resourceId);
        if (name == null) {
            name = context.getResources().getResourceEntryName(resourceId);
            resourceIdAndNames.put(resourceId, name);
        }
        return loadTexture(name, context, resourceId);
    }

    /**
     * Load textures from memory if available, otherwise, create a new texture handle, store and return
     * @param name A string "handle" that can be referenced if this is drawn again
     * @param context An Android activity
     * @param resourceId A resource "handle" such as R.drawable.usb_android
     * @return the previously generated texture handle, or a new one
     */
	public static int loadTexture(final String name, final Context context, final int resourceId)
	{
        if (texturesByName.containsKey(name)) {
            return texturesByName.get(name);
        }
        if (resourceId == 0) {
            return loadTexture("usb_android", context, R.drawable.usb_android);
        }
		final int[] textureHandle = new int[1];
		GLES20.glGenTextures(1, textureHandle, 0);

		if (textureHandle[0] != 0) {
			bindBitmap(BitmapHelper.findBitmapOrBuild(resourceId), textureHandle[0]);
		}
		else {
            System.err.println(name + " " + resourceId + " " + context.getResources().getResourceEntryName(resourceId));
			throw new RuntimeException("Error loading texture.");
		}

        texturesByName.put(name, textureHandle[0]);
		return textureHandle[0];
	}

    /*public static int loadTintedTexture(final String name, final Context context, final int resourceId, int[] tintColor)
    {
        int intFromColor;
        if (tintColor.length == 3) {
            intFromColor = ColorTextureHelper.intFromColor(tintColor[0], tintColor[1], tintColor[2], 255);
        }
        else {
            intFromColor = ColorTextureHelper.intFromColor(tintColor[0], tintColor[1], tintColor[2], tintColor[3]);
        }
        String tintedBitmapName = name + "/rgb" + intFromColor;
        System.out.println("Load tinted texture: " + tintedBitmapName);
        if (texturesByName.containsKey(name)) {
            return texturesByName.get(name);
        }
        System.out.println("Creating tinted texture: " + tintedBitmapName);
        final int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            Bitmap bitmap = BitmapHelper.getBitmap(tintedBitmapName);
            if (bitmap != null) {
                //Found the tinted version of the bitmap, don't generate a new one
            }
            else {
                bitmap = BitmapHelper.findBitmapOrBuild(resourceId).copy(Bitmap.Config.ARGB_8888, true);
                for (int x = 0; x < bitmap.getWidth(); x++) {
                    for (int y = 0; y < bitmap.getHeight(); y++) {
                        int originalColor = bitmap.getPixel(x, y);
                        int b = originalColor & 0x000000FF;
                        int g = (originalColor >>> 8) & 0x000000FF;
                        int r = (originalColor >>> 16) & 0x000000FF;
                        int a = (originalColor >>> 24) & 0x000000FF;

                        int tintR = r + (int) ((tintColor[0] - r) * 0.5f);
                        int tintG = g + (int) ((tintColor[1] - g) * 0.5f);
                        int tintB = b + (int) ((tintColor[2] - b) * 0.5f);

                        int newColor = ColorTextureHelper.intFromColor(tintR, tintG, tintB, a);

                        bitmap.setPixel(x, y, newColor);
                    }
                }
                BitmapHelper.addBitmap(tintedBitmapName, bitmap);
            }
            bindBitmap(bitmap, textureHandle[0]);
        }
        else {
            throw new RuntimeException("Error loading tinted texture.");
        }

        texturesByName.put(name, textureHandle[0]);
        return textureHandle[0];
    }*/

    /**
     * Load textures from memory if available, otherwise, create a new texture handle from a Bitmap
     * name A string "handle" that can be referenced if this is drawn again
     * bitmap A bitmap, either generated or from another external source
     * @return the previously generated texture handle, or a new one
     */
    public static int loadTexture(final String name, final Bitmap bitmap) {
        if (texturesByName.containsKey(name)) {
            return texturesByName.get(name);
        }
        final int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;	// No pre-scaling
            bindBitmap(bitmap, textureHandle[0]);
        }
        else {
            throw new RuntimeException("Error loading texture: " + name);
        }

        bitmap.recycle();

        texturesByName.put(name, textureHandle[0]);
        return textureHandle[0];
    }

    /*
    Bind a new texture to the texture handle from the Bitmap, GC the used Bitmap
     */
    public static void bindBitmap(Bitmap bitmap, int textureHandle) {
        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        // Recycle the bitmap, since its data has been loaded into OpenGL.
        bitmap.recycle();
    }
}
