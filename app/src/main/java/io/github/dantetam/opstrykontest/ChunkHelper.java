package io.github.dantetam.opstrykontest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.github.dantetam.world.Tile;
import io.github.dantetam.world.World;

/**
 * Created by Dante on 7/18/2016.
 */
public class ChunkHelper {

    private World world;
    private Node root;

    private Tile[][] alignedTiles;
    public static final int CHUNK_TILE_SIZE = 8;

    public Node[][] nodesArray; //For quickly finding immediate neighbors (indiced)
    private List<Node> allLeafNodes = new ArrayList<>();

    public ChunkHelper() {
        //nodesArray = new ArrayList<>();
    }

    public void init(World w) {
        world = w;
        makeAlignedTiles(world);
        root = splitAllAlignedTiles();
    }

    private void makeAlignedTiles(World w) {
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

    public Node splitAllAlignedTiles() {
        return split(0, 0, alignedTiles.length, alignedTiles[0].length);
    }
    private Node split(int startX, int startZ, int endX, int endZ) {
        allLeafNodes.clear();
        float floatX = (startX + endX) / 2f, floatZ = (startZ + endZ) / 2f;
        int midX = (int) floatX, midZ = (int) floatZ;
        Node node = new Node(startX, startZ, endX, endZ, floatX, floatZ);
        if (endX - startX > CHUNK_TILE_SIZE && endZ - startZ > CHUNK_TILE_SIZE) {
            node.children = new Node[4];
            node.children[0] = split(startX, startZ, midX, midZ);
            node.children[1] = split(midX, startZ, endX, midZ);
            node.children[2] = split(startX, midZ, midX, endZ);
            node.children[3] = split(midX, midZ, endX, endZ);
        }
        else {
            //System.out.println("Box: " + floatX + " " + floatZ);
            node.tiles = new ArrayList<>();
            for (int x = startX; x < endX; x++) {
                for (int z = startZ; z < endZ; z++) {
                    node.tiles.add(alignedTiles[x][z]);
                }
            }
            allLeafNodes.add(node);
        }
        return node;
    }

    public Node findNodeContainingTile(Tile tile) {
        return findNodeContainingTile(root, tile);
    }
    private Node findNodeContainingTile(Node inspect, Tile tile) {
        if (inspect.children == null) {
            if (inspect.tiles.contains(tile)) {
                return inspect;
            }
            return null;
        }
        else {
            float dx = tile.q - inspect.centerX;
            float dz = tile.r - inspect.centerZ;
            if (dx < 0 && dz < 0) {
                return findNodeContainingTile(inspect.children[0], tile);
            }
            else if (dx > 0 && dz < 0) {
                return findNodeContainingTile(inspect.children[1], tile);
            }
            else if (dx < 0 && dz > 0) {
                return findNodeContainingTile(inspect.children[2], tile);
            }
            else {
                return findNodeContainingTile(inspect.children[3], tile);
            }
        }
    }

    /*private List<Node> getAllLeafNodes() {
        allLeafNodes.clear();
        getAllLeafNodes(root);
        return allLeafNodes;
    }
    private void getAllLeafNodes(Node inspect) {
        if (inspect.children == null) {
            allLeafNodes.add(inspect);
        }
        else {
            for (int i = 0; i < inspect.children.length; i++) {
                getAllLeafNodes(inspect.children[i]);
            }
        }
    }*/

    /*public void convertToListToArray() {
        HashSet<Float> uniqueEntriesX = new HashSet<>(), uniqueEntriesZ = new HashSet<>();
        for (Node node: allLeafNodes) {
            uniqueEntriesX.add(node.centerX);
            uniqueEntriesZ.add(node.centerZ);
        }
        nodesArray = new Node[uniqueEntriesX.size()][uniqueEntriesZ.size()];
        for (Node node: allLeafNodes) {

        }
    }*/

    public class Node {
        public Node[] children;
        public int topLeftX, topLeftZ, bottomRightX, bottomRightZ;
        public float centerX, centerZ;
        public List<Tile> tiles = null;

        public Node(int a, int b, int c, int d, float e, float f) {
            topLeftX = a;
            topLeftZ = b;
            bottomRightX = c;
            bottomRightZ = d;
            centerX = e;
            centerZ = f;
        }
    }

}
