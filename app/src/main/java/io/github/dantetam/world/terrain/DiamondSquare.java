package io.github.dantetam.world.terrain;


public class DiamondSquare extends BaseTerrain {

    //public double[][] terrain;
    public boolean positiveOnly;

	public static void main(String[] args)
    {
		double[][] temp = makeTable(50,50,50,50,17);
		DiamondSquare ds = new DiamondSquare(temp);
		//ds.diamond(0, 0, 4);
		ds.dS(0, 0, 16, 15, 0.5, true);
	}

    public DiamondSquare() {

    }

    public DiamondSquare(double[][] start) {
        init(start);
    }

    public void init(double[][] start) {
        terrain = start;
        forceStay = new boolean[start.length][start[0].length];
        for (int r = 0; r < start.length; r++) {
            for (int c = 0; c < start[0].length; c++) {
                if (start[r][c] != 0) {
                    forceStay[r][c] = true;
                }
            }
        }
    }

    //Creates a table with 4 corners set to argument values
    public static double[][] makeTable(double topLeft, double topRight, double botLeft, double botRight, int width) {
        double[][] temp = new double[width][width];
        for (int r = 0; r < width; r++) {
            for (int c = 0; c < width; c++) {
                temp[r][c] = 0;
            }
        }
        temp[0][0] = topLeft;
        temp[0][width - 1] = topRight;
        temp[width - 1][0] = botLeft;
        temp[width - 1][width - 1] = botRight;
        return temp;
    }

    public static void printTable(double[][] a) {
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                System.out.print((int) a[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static double[][] boundMax(double[][] t, double maxHeight) {
        double[][] temp = new double[t.length][t[0].length];
        for (int r = 0; r < temp.length; r++) {
            for (int c = 0; c < temp[0].length; c++) {
                if (t[r][c] > maxHeight)
                    temp[r][c] = maxHeight;
                else
                    temp[r][c] = t[r][c];
            }
        }
        return temp;
    }

    /*
     t = {
     3 6 0
     3 0 3
     9 6 0
     }
     normalize(t, 9, 1) -> t = {1/3, 2/3, 0, 1/3, 0, 1/3, 1, 2/3, 0}
     normalize(t, 9, 0) -> t = {0 0 0 ... 0}
     */
    public static double[][] normalize(double[][] t, double maxHeight, double newMax) {
        return null;
    }

    //Starts the iterative loop over the terrain that modifies it
    //Returns a list of the tables between each diamond-square cycle
    public double[][] dS(int sX, int sY, int width, double startAmp, double ratio, boolean positiveOnly) {
        int origWidth = width;
        this.positiveOnly = positiveOnly;
        while (true) {
            for (int r = sX; r <= terrain.length - 2; r += width) {
                for (int c = sY; c <= terrain[0].length - 2; c += width) {
                    diamond(r, c, width, startAmp);
                }
            }
            if (width > 1) {
                width /= 2;
                startAmp *= ratio;
            } else
                break;
        }
        return terrain;
    }

    public boolean[][] forceStay;

    public void diamond(int sX, int sY, int width, double startAmp) {
        //System.out.println(random);
        if (!forceStay[sX + width / 2][sY + width / 2])
            terrain[sX + width / 2][sY + width / 2] = (terrain[sX][sY] + terrain[sX + width][sY] + terrain[sX][sY + width] + terrain[sX + width][sY + width]) / 4;
        if (!positiveOnly)
            terrain[sX + width / 2][sY + width / 2] += startAmp * (random.nextDouble() - 0.5) * 2;
        else
            terrain[sX + width / 2][sY + width / 2] += startAmp * random.nextDouble() * 2;
		/*System.out.println(t[sX][sY]);
		System.out.println(t[sX+width][sY]);
		System.out.println(t[sX][sY+width]);
		System.out.println(t[sX+width][sY+width]);
		System.out.println("-------");*/
        //printTable(t);
        //System.out.println("-------");
        if (width > 1) {
            square(sX + width / 2, sY, width, startAmp);
            square(sX, sY + width / 2, width, startAmp);
            square(sX + width, sY + width / 2, width, startAmp);
            square(sX + width / 2, sY + width, width, startAmp);
            //diamond(sX, sY, width/2);
            //diamond(sX + width/2, sY, width/2);
            //diamond(sX, sY + width/2, width/2);
            //diamond(sX + width/2, sY + width/2, width/2);
        }
    }

    public void square(int sX, int sY, int width, double startAmp) {
        if (forceStay[sX][sY]) return;
        //Cases 1-5
        if (sX - width / 2 < 0)
            terrain[sX][sY] = (terrain[sX][sY - width / 2] + terrain[sX][sY + width / 2] + terrain[sX + width / 2][sY]) / 3;
        else if (sX + width / 2 >= terrain.length)
            terrain[sX][sY] = (terrain[sX][sY - width / 2] + terrain[sX][sY + width / 2] + terrain[sX - width / 2][sY]) / 3;
        else if (sY - width / 2 < 0)
            terrain[sX][sY] = (terrain[sX][sY + width / 2] + terrain[sX + width / 2][sY] + terrain[sX - width / 2][sY]) / 3;
        else if (sY + width / 2 >= terrain.length)
            terrain[sX][sY] = (terrain[sX][sY - width / 2] + terrain[sX + width / 2][sY] + terrain[sX - width / 2][sY]) / 3;
        else
            terrain[sX][sY] = (terrain[sX][sY + width / 2] + terrain[sX][sY - width / 2] + terrain[sX + width / 2][sY] + terrain[sX - width / 2][sY]) / 4;
        if (!positiveOnly)
            terrain[sX][sY] += startAmp * (random.nextDouble() - 0.5) * 2;
        else
            terrain[sX][sY] += startAmp * random.nextDouble() * 2;
    }

    public double[][] generate(double[][] begin, double[] args) {
        //seed(870);
        init(begin);
        return generate(args);
    }

    public double[][] generate(double[] args) {
        return dS((int) args[0], (int) args[1], (int) args[2], args[3], args[4], true);
    }

}
