package io.github.dantetam.world.terrain;

/**
 * Created by Dante on 9/24/2016.
 */
public class TerrainUtil {

    public static double[][] cutoff(double[][] t, double cutoff) {
        double[][] temp = new double[t.length][t[0].length];
        for (int r = 0; r < t.length; r++) {
            for (int c = 0; c < t[0].length; c++) {
                temp[r][c] = t[r][c] - cutoff;
            }
        }
        return temp;
    }

    public static double[][] recurInter(double[][] source, int times, double nDiv) {
        if (times < 0) {
            return source;
        }
        return recurInter(expand(source, nDiv), times - 1, nDiv);
    }

    public static double[][] expand(double[][] a, double nDiv) {
        BicubicInterpolator bi = new BicubicInterpolator();
        double[][] returnThis = new double[(int) nDiv][(int) nDiv];
        for (int i = 0; i < nDiv; i++) {
            for (int j = 0; j < nDiv; j++) {
                double idx = (double) (a.length * i) / nDiv;
                double idy = (double) (a[0].length * j) / nDiv;
                //System.out.println("L: " + idx + "," + idy + ": " + bi.getValue(source,idx,idx));
                double zeroCheck = bi.getValue(a, idx, idy);
                returnThis[i][j] = zeroCheck >= 0 ? zeroCheck : 0;
            }
        }
        return returnThis;
    }

    public static double[][] positiveTable(double[][] a) {
        double[][] b = new double[a.length][a[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                b[i][j] = Math.abs(a[i][j]);
            }
        }
        return b;
    }

    public static double[][] scalar(double ratio, double[][] a) {
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                a[i][j] *= ratio;
            }
        }
        return a;
    }

    public static <K> void printTable(K[][] a) {
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                System.out.print(a[i][j].toString() + " ");
            }
            System.out.println();
        }
    }

    public static void printIntTable(int[][] a) {
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                System.out.print(a[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void printDoubleTable(double[][] a) {
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                System.out.print((int) a[i][j] + " ");
            }
            System.out.println();
        }
    }

}
