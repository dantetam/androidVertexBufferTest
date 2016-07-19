package io.github.dantetam.opstrykontest;

import java.util.ArrayList;
import java.util.List;

import io.github.dantetam.world.Tile;
import io.github.dantetam.world.World;

/**
 * Created by Dante on 7/18/2016.
 */
public class ChunkHelper {

    private World world;

    public Tile[][] alignedTiles;
    public static final int CHUNK_TILE_SIZE = 8;

    public ChunkHelper(World w) {
        world = w;
    }

    public void makeAlignedTiles(World w) {
        Tile[][] unaligned = w.returnTilesForChunking();
        //Count tiles in first row
        int length = 0;
        for (int i = 0; i < unaligned[0].length; i++) {
            if (unaligned[0][i] != null) {
                length++;
            }
        }
        alignedTiles = new Tile[unaligned.length][length];
        for (int x = 0; x < unaligned.length; x++) {
            length = 0;
            for (int z = 0; z < unaligned[x].length; z++) {
                if (unaligned[x][z] != null) {
                    alignedTiles[x][length] = unaligned[x][z];
                    length++;
                }
            }
        }
    }

    public void splitStart() {
        split(0, 0, alignedTiles.length, alignedTiles[0].length);
    }
    public Node split(int startX, int startZ, int endX, int endZ) {
        Node node = new Node(startX, startZ, endX, endZ);
        int midX = (startX + endX) / 2, midZ = (startZ + endZ) / 2;
        if (endX - startX > CHUNK_TILE_SIZE && endZ - startZ > CHUNK_TILE_SIZE) {
            node.children[0] = split(startX, startZ, midX, midZ);
            node.children[1] = split(midX, startZ, endX, midZ);
            node.children[2] = split(startX, midZ, midX, endZ);
            node.children[3] = split(midX, midZ, endX, endZ);
        }
        else {
            for (int x = startX; x < endX; x++) {
                for (int z = startZ; z < endZ; z++) {
                    node.tiles.add(alignedTiles[x][z]);
                }
            }
        }
        return node;
    }

    public class Node {
        public Node[] children;
        public int topLeftX, topLeftZ, bottomRightX, bottomRightZ;
        public List<Tile> tiles = null;

        public Node(int a, int b, int c, int d) {
            children = new Node[4];
            topLeftX = a;
            topLeftZ = b;
            bottomRightX = c;
            bottomRightZ = d;
            tiles = new ArrayList<>();
        }
    }

}
