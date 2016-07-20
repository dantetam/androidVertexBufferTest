package io.github.dantetam.opstrykontest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.dantetam.world.Person;
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

    public HashMap<Float, List<Node>> nodesList; //For quickly finding immediate neighbors (indiced)
    private List<Node> allLeafNodes = new ArrayList<>();
    public Node[][] nodesArray;

    public ChunkHelper() {
        //nodesArray = new ArrayList<>();
        nodesList = new HashMap<>();
    }

    public void init(World w) {
        world = w;
        makeAlignedTiles(world);
        root = splitAllAlignedTiles();
        int i = 0;
        for (Map.Entry<Float, List<Node>> en: nodesList.entrySet()) {
            List<Node> nodes = en.getValue();
            if (nodesArray == null) {
                nodesArray = new Node[nodesList.size()][nodes.size()];
            }
            for (int j = 0; j < nodes.size(); j++) {
                nodesArray[i][j] = nodes.get(j);
                nodesArray[i][j].arrayPosX = i;
                nodesArray[i][j].arrayPosZ = j;
            }
            i++;
            /*System.out.println(en.getKey());
            String stringy = "";
            for (Node node: nodes) {
                stringy += node.centerZ;
                stringy += " ";
            }
            System.out.println(stringy);*/
        }
    }

    public List<Tile> getChunkTiles(Tile t, int radius) {
        Node home = findNodeContainingTile(t);
        Collection<Node> radiusChunks = getChunksWithinRadius(home, radius);
        List<Tile> tiles = new ArrayList<>();
        for (Node node: radiusChunks) {
            for (Tile tile: node.tiles) {
                tiles.add(tile);
            }
        }
        return tiles;
    }

    private Collection<Node> getChunksWithinRadius(Node t, int radius) {
        Set<Node> rings = new HashSet<>();
        if (radius == 1) {
            Collection<Node> tiles = neighbors(nodesArray, t.arrayPosX, t.arrayPosZ);
            tiles.add(t);
            return tiles;
        }
        rings.add(t);
        if (radius > 0) {
            for (Node neighbor: neighbors(nodesArray, t.arrayPosX, t.arrayPosZ)) {
                Collection<Node> neighborRing = getChunksWithinRadius(neighbor, radius - 1);
                for (Node neighborRingTile: neighborRing) {
                    rings.add(neighborRingTile);
                }
            }
        }
        return rings;
    }

    private static final int[][] neighborOffsets = {
            {1,0},
            {-1,0},
            {0,1},
            {0,-1},
            {1,1},
            {-1,-1},
            {1,-1},
            {-1,1},
    };
    public Set<Node> neighbors(Node[][] nodes, int r, int c) {
        Set<Node> neighborNodes = new HashSet<>();
        for (int[] offset: neighborOffsets) {
            if (inBounds(nodes, r + offset[0], c + offset[1])) {
                neighborNodes.add(nodes[r + offset[0]][c + offset[1]]);
            }
        }
        return neighborNodes;
    }

    private boolean inBounds(Node[][] nodes, int r, int c) {
        return r >= 0 && c >= 0 && r < nodes.length && c < nodes[0].length;
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
            if (nodesList.get(node.centerX) == null) {
                List<Node> nodes = new ArrayList<>();
                nodes.add(node);
                nodesList.put(node.centerX, nodes);
            }
            else {
                nodesList.get(node.centerX).add(node);
            }
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
        public int arrayPosX, arrayPosZ;
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
