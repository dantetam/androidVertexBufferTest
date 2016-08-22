package io.github.dantetam.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class RawResourceReader
{
    /*
    Return an entire string of data from an Android resource
     */
	public static String loadStringOfText(final Context context,
			final int resourceId)
	{
		final InputStream inputStream = context.getResources().openRawResource(
				resourceId);
		final InputStreamReader inputStreamReader = new InputStreamReader(
				inputStream);
		final BufferedReader bufferedReader = new BufferedReader(
				inputStreamReader);

		String nextLine;
		final StringBuilder body = new StringBuilder();

		try
		{
			while ((nextLine = bufferedReader.readLine()) != null)
			{
				body.append(nextLine);
				body.append('\n');
			}
		}
		catch (IOException e)
		{
			return null;
		}

		return body.toString();
	}

    /**
     * Load strings from some sort of text file.
     * @param context An activity
     * @param resourceId A resource "handle" such as R.drawable.usb_android (presumably an OBJ)
     * @return a list of all the parsed strings
     */
    public static List<String> loadListOfText(final Context context, final int resourceId)
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

}
