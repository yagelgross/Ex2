public class CellEntry implements Index2D {
    private final int row;    // 0-based row index
    private final int column; // 0-based column index

    public CellEntry(String reference) {
        // Validate that the reference matches the format: single letter + number (e.g., A0, B2)
        if (!reference.matches("[A-Za-z]\\d+")) {
            throw new IllegalArgumentException("Invalid cell reference: " + reference);
        }

        // Extract the column letter and row number
        String column = reference.substring(0, 1); // First character (capital letter for column)
        String row = reference.substring(1);       // Remaining characters (digits for row)

        this.column = column.charAt(0) - 'A';      // Convert column letter to 0-based index
        this.row = Integer.parseInt(row);         // Row is already 0-based
    }
    public CellEntry(int x, int y) {
        this.column = x;
        this.row = y;
    }

    public static String toCellRef(int x, int y) {
        String s;
        if (x<0 || x>25 || y<0 || y>99) {
            s = Ex2Utils.ERR_FORM;
        }
        else {
            s = (char) ('A' + x) + Integer.toString(y);
        }
        return s;
    }

    public int getColumn() {
        return this.column; // Return 0-based column index
    }

    @Override
    public boolean isValid() {
        boolean valid = false;
        if (this.toString().matches("[A-Z][0-9]+"))
        {
            valid = true;
        }
        else if (this.toString().matches("[a-z][0-9]+"))
        {
            valid = true;
        }
        if (this.toString().length()>3)
        {
            valid = false;
        }
        return valid;
    }

    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getY() {
        return 0;
    }

    public int getRow() {
        return row;
    }
}
