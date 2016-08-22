package io.github.dantetam.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

/**
 * Created by Dante on 7/19/2016.
 */
public class BitmapHelper {

    private static LruCache<String, Bitmap> mMemoryCache;
    private static Context context;

    public static void init(Context c) {
        context = c;
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 6;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public static void addBitmap(String key, Bitmap bitmap) {
        if (getBitmap(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public static Bitmap getBitmap(String key) {
        return mMemoryCache.get(key);
    }

    public static Bitmap findBitmapOrBuild(int resourceId) {
        String name = context.getResources().getResourceEntryName(resourceId);
        Bitmap bitmapStored = getBitmap(name);
        if (bitmapStored != null) {
            return bitmapStored;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;	// No pre-scaling
        options.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        return bitmap;
    }

}
