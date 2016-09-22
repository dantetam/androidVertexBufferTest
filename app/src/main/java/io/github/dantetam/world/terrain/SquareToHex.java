package io.github.dantetam.world.terrain;

import java.util.ArrayList;

import io.github.dantetam.world.entity.Item;
import io.github.dantetam.world.entity.Tile;
import io.github.dantetam.world.factory.TileFactory;

/**
 * Created by Dante on 9/22/2016.
 */
public class SquareToHex {

    public static void main(String[] args) {
        double[][] temp = DiamondSquare.makeTable(50,50,50,50,17);
        DiamondSquare ds = new DiamondSquare(temp);
        //ds.diamond(0, 0, 4);
        double[][] squareArray = ds.dS(0, 0, 16, 15, 0.5, true);

        double[][] worldHexArray = new double[16][16];
        mutateHexArray(squareArray, worldHexArray, 12, 12);
    }

    public static void mutateHexArray(double[][] squareArray, double[][] worldHexArray, int totalX, int totalZ) {
        int arrayLengthX = worldHexArray.length;
        int arrayLengthZ = worldHexArray[0].length;
        int startingZ = arrayLengthZ - 1;
        int i = 0, j = 0;
        for (int x = 0; x < arrayLengthX; x++) {
            for (int z = startingZ; z >= startingZ - totalZ; z--) {
                worldHexArray[x][z] = squareArray[i][j];
                j++;
            }
            j = 0;
            i++;
            if (x % 2 == 1) {
                startingZ--;
            }
        }
    }

}
