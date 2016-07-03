package io.github.dantetam.opstrykontest;

import java.util.List;
import java.util.Vector;

/**
 * Created by Dante on 7/2/2016.
 * Library code. Used to determine if a point lies within a constructed polygon
 */
public class Polygon
{
    private float[] polyX, polyY;
    private int polySides;

    /**
     * @param px,py Array of points
     * @param ps Number of sides
     */
    public Polygon(float[] px, float[] py, int ps)
    {
        polyX = px;
        polyY = py;
        polySides = ps;
    }

    public Polygon(List<Vector2f> points) {
        polySides = points.size();
        polyX = new float[points.size()];
        for (int i = 0; i < polySides; i++) {
            polyX[i] = points.get(i).x;
            polyY[i] = points.get(i).y;
        }
    }

    /**
     * Checks if the Polygon contains a point.
     * @see "http://alienryderflex.com/polygon/"
     */
    public boolean contains(float x, float y)
    {
        boolean c = false;
        int i, j = 0;
        for (i = 0, j = polySides - 1; i < polySides; j = i++) {
            if (((polyY[i] > y) != (polyY[j] > y))
                    && (x < (polyX[j] - polyX[i]) * (y - polyY[i]) / (polyY[j] - polyY[i]) + polyX[i]))
                c = !c;
        }
        return c;
    }

}
