import java.io.IOException;
// Add your documentation below:

public class Ex2Sheet implements Sheet {
    private Cell[][] table;
    // Add your code here

    // ///////////////////
    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for(int i=0;i<x;i=i+1) {
            for(int j=0;j<y;j=j+1) {
                table[i][j] = new SCell("");
            }
        }
        eval();
    }
    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Override
    public String value(int x, int y) {
        String ans = Ex2Utils.EMPTY_CELL;

        Cell c = get(x,y);
        if(c!=null) {ans = c.toString();}

        return ans;
    }

    @Override
    public Cell get(int x, int y) {
        return table[x][y];
    }

    @Override
    public Cell get(String cords) {
        Cell ans = null;

        // Validate input
        if (cords == null || cords.isEmpty()) {
            return null;
        }

        try {
            // Parse the letter portion (e.g., 'A' -> 0, 'B' -> 1, etc.)
            char letterPart = Character.toUpperCase(cords.charAt(0));
            int x = letterPart - 'A'; // Maps 'A' to 0, 'B' to 1, etc.

            // Parse the number portion (e.g., '1' -> 0 in zero-based index)
            int y = Integer.parseInt(cords.substring(1)) - 1;

            // Check bounds
            if (isIn(x, y)) { // Use existing isIn(int xx, int yy) for bounds checking
                ans = get(x, y); // Retrieve the Cell
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ans;
    }

    @Override
    public int width() {
        return table.length;
    }
    @Override
    public int height() {
        return table[0].length;
    }
    @Override
    public void set(int x, int y, String s) {
        Cell c = new SCell(s);
        table[x][y] = c;



    }
    @Override
    public void eval() {
        int[][] dd = depth();
        // Add your code here

        // ///////////////////
    }

    @Override
    public boolean isIn(int xx, int yy) {
        return xx >= 0 && yy >= 0 && xx < width() && yy < height();
    }

    @Override
    public int[][] depth() {
        int[][] ans = new int[width()][height()];
        // Add your code here

        // ///////////////////
        return ans;
    }

    @Override
    public void load(String fileName) throws IOException {
        // Add your code here

        // ///////////////////
    }

    @Override
    public void save(String fileName) throws IOException {
        // Add your code here

        // ///////////////////
    }

    @Override
    public String eval(int x, int y) {
        String ans = null;
        if(get(x,y)!=null) {ans = get(x,y).toString();}
        // Add your code here

        // ///////////////////
        return ans;
        }
}
