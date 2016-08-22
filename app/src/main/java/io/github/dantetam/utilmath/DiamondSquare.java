package io.github.dantetam.utilmath;

import java.util.Random;

/**
 * Created by Dante on 6/17/2016.
 * This is intended as a class to make arbitrarily sized pieces of terrain data.
 * TODO: Import interpolation from past project
 */
public class DiamondSquare {

    private Random random;
    private int desiredWidth, desiredHeight, usedWidth;
    private double amp, per;

    public DiamondSquare(int width, double amp, double per) {
        this(width, width, amp, per);
    }

    public DiamondSquare(int width, int height, double amp, double per) {
        this.amp = amp;
        this.per = per;

        desiredWidth = width;
        desiredHeight = height;
        double raw = Math.log(Math.max(width, height) - 1) / Math.log(2);
        if (raw == Math.round(raw))
            usedWidth = width;
        else
            usedWidth = (int) Math.pow(2, (int) raw + 1) + 1;

        System.out.println(desiredWidth + " " + usedWidth);
    }

    public double[][] getTerrain(double minAmp, double maxAmp) {
        double[][] temp = makeTable(0,0,0,0,usedWidth);
        dS(temp, 0, 0, usedWidth - 1, amp, per);
        /*if (temp.length == desiredWidth && temp[0].length == desiredHeight) {
            return temp;
        }
        else {
            double[][] returnThis = new double[desiredWidth][desiredHeight];
            for (int r = 0; r < desiredWidth; r++) {
                for (int c = 0; c < desiredHeight; c++) {
                    returnThis[r][c] = temp[r][c];
                }
            }
            return returnThis;
        }*/
        double[][] returnThis = new double[desiredWidth][desiredHeight];
        for (int r = 0; r < desiredWidth; r++) {
            for (int c = 0; c < desiredHeight; c++) {
                returnThis[r][c] = Math.max(minAmp, Math.min(temp[r][c], maxAmp));
            }
        }
        return returnThis;
    }

    public int[][] getIntTerrain(int a, int b) {
        return toIntTable(getTerrain(a,b));
    }

    public static int[][] toIntTable(double[][] t) {
        if (t.length == 0) return new int[][]{};
        int[][] result = new int[t.length][t[0].length];
        for (int r = 0; r < t.length; r++) {
            for (int c = 0; c < t[0].length; c++) {
                result[r][c] = (int) t[r][c];
            }
        }
        return result;
    }

    public DiamondSquare seed(long s) {
        random = new Random();
        random.setSeed(s);
        return this;
    }

    public static double[][] makeTable(double topLeft, double topRight, double botLeft, double botRight, int width)
    {
        double[][] temp = new double[width][width];
        for (int r = 0; r < width; r++) {
            for (int c = 0; c < width; c++) {
                temp[r][c] = 0;
            }
        }
        temp[0][0] = topLeft;
        temp[0][width-1] = topRight;
        temp[width-1][0] = botLeft;
        temp[width-1][width-1] = botRight;
        return temp;
    }

    public double[][] dS(double[][] t, int sX, int sY, int width, double startAmp, double ratio)
    {
        while (true)
        {
            for (int r = sX; r <= t.length - 2; r += width)
            for (int c = sY; c <= t[0].length - 2; c += width)
            diamond(t, r, c, width, startAmp);
            if (width > 1)
            {
                width /= 2;
                startAmp *= ratio;
            }
            else
                break;
        }
        return t;
    }

    private void diamond(double[][] t, int sX, int sY, int width, double startAmp)
    {
        t[sX + width/2][sY + width/2] = (t[sX][sY] + t[sX+width][sY] + t[sX][sY+width] + t[sX+width][sY+width])/4;
        t[sX + width/2][sY + width/2] += startAmp*(random.nextDouble() - 0.5)*2;
        if (width > 1) {
            square(t, sX + width/2, sY, width, startAmp);
            square(t, sX, sY + width/2, width, startAmp);
            square(t, sX + width, sY + width/2, width, startAmp);
            square(t, sX + width/2, sY + width, width, startAmp);
        }
    }

    private void square(double[][] t, int sX, int sY, int width, double startAmp)
    {
        if (sX - width/2 < 0)
            t[sX][sY] = (t[sX][sY - width/2] + t[sX][sY + width/2] + t[sX + width/2][sY])/3;
        else if (sX + width/2 >= t.length)
            t[sX][sY] = (t[sX][sY - width/2] + t[sX][sY + width/2] + t[sX - width/2][sY])/3;
        else if (sY - width/2 < 0)
            t[sX][sY] = (t[sX][sY + width/2] + t[sX + width/2][sY] + t[sX - width/2][sY])/3;
        else if (sY + width/2 >= t.length)
            t[sX][sY] = (t[sX][sY - width/2] + t[sX + width/2][sY] + t[sX - width/2][sY])/3;
        else
            t[sX][sY] = (t[sX][sY + width/2] + t[sX][sY - width/2] + t[sX + width/2][sY] + t[sX - width/2][sY])/4;
        t[sX][sY] += startAmp*(random.nextDouble() - 0.5)*2;
    }

}
