package io.github.dantetam.opstrykontest;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dante on 7/15/2016.
 */
public class FileParser {

    public static LessonSevenActivity mActivity;

    public FileParser(LessonSevenActivity lessonSevenActivity) {
        mActivity = lessonSevenActivity;
    }

    /**
     * Load strings from some sort of text file.
     * @param context An activity
     * @param resourceId A resource "handle" such as R.drawable.usb_android (presumably an OBJ)
     * @return a list of all the parsed strings
     */
    public static List<String> loadText(final Context context, final int resourceId)
    {
        final InputStream inputStream = context.getResources().openRawResource(
                resourceId);
        final InputStreamReader inputStreamReader = new InputStreamReader(
                inputStream);

        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line;
        List<String> parsed = new ArrayList<>();

        try {
            line = reader.readLine();
            while (line != null) {
                parsed.add(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {e.printStackTrace();}

        return parsed;
    }
    public static List<String> loadText(final int resourceId) {
        return loadText(mActivity, resourceId);
    }

}
