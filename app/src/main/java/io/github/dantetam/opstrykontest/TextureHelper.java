package io.github.dantetam.opstrykontest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.util.HashMap;

/*
A helper class which is solely responsible for loading all textures.
All textures must be loaded here so their names can be stored in the hash map.
 */
public class TextureHelper
{
    //Stores name of texture and its respective OpenGL handle
    public static HashMap<String, Integer> texturesByName = new HashMap<>();

    /*
    Load a texture and look it up solely by name.
     */
    public static int loadTexture(final String name) {
        if (texturesByName.containsKey(name)) {
            return texturesByName.get(name);
        }
        return -1;
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
		final int[] textureHandle = new int[1];
		GLES20.glGenTextures(1, textureHandle, 0);

		if (textureHandle[0] != 0) {
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;	// No pre-scaling
			final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
			bindBitmap(bitmap, textureHandle[0]);
		}
		else {
			throw new RuntimeException("Error loading texture.");
		}

        texturesByName.put(name, textureHandle[0]);
		return textureHandle[0];
	}

    /**
     * Load textures from memory if available, otherwise, create a new texture handle from a Bitmap
     * @param name A string "handle" that can be referenced if this is drawn again
     * @param bitmap A bitmap, either generated or from another external source
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
