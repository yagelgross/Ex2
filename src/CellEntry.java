public class CellEntry implements Index2D {
    private final int x; // Column index (0-based)
    private final int y; // Row index (0-based)

    public CellEntry(String cellRef) {
        this.x = getCol(cellRef);
        this.y = getRow(cellRef);
    }

    public CellEntry(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return (char) ('A' + x) + Integer.toString(y);
    }

    @Override
    public boolean isValid() {
        return x >= 0 && x <= 25 && y >= 0 && y <= 99;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    // Utility methods to translate "B2" to coordinates and back
    public static int getRow(String cellRef) {
        return Integer.parseInt(cellRef.replaceAll("[^0-9]", "")) - 1;
    }

    public static int getCol(String cellRef) {
        return cellRef.toUpperCase().charAt(0) - 'A';
    }

    public static String toCellRef(int x, int y) {
        return (char) ('A' + x) + Integer.toString(y + 1);
    }
}