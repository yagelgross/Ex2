public class CellReferenceResolver {
    private static final int MAX_ROWS = 100; // Maximum allowed rows (0-99)
    private static final int MAX_COLUMNS = 26; // Maximum allowed columns (A-Z)

    /**
     * Resolves a cell reference (e.g., "D0") into its zero-based row and column indices.
     * Throws an exception for invalid or out-of-bound references.
     *
     * @param reference The cell reference to resolve (e.g., "D0").
     * @return A 2D coordinate array where [0] = row, [1] = column.
     */
    public static int[] resolveCellReference(String reference) {
        if (reference == null || reference.trim().isEmpty()) {
            throw new IllegalArgumentException("Cell reference cannot be null or empty.");
        }

        // Extract the column (letters) and row (digits) parts
        String columnPart = reference.substring(0,1);
        String rowPart = reference.substring(1);

        if (columnPart.isEmpty() || rowPart.isEmpty()) {
            throw new IllegalArgumentException("Cell reference format is invalid: " + reference);
        }

        // Convert column from letters (A-Z) to a numerical index (0-based)
        int column = columnToIndex(columnPart);

        // Parse row and ensure it is within bounds
        int row = Integer.parseInt(rowPart);

        if (row < 0 || row >= MAX_ROWS || column < 0 || column >= MAX_COLUMNS) {
            throw new IllegalArgumentException("Invalid reference (out of bounds): " + reference);
        }

        // Return as a 0-based index for internal processing
        return new int[]{column, row};
    }

    /**
     * Converts an alphabetical column reference (e.g., A, B, Z) into a zero-based index.
     *
     * @param column The column reference in letters.
     * @return The zero-based column index.
     */
    private static int columnToIndex(String column) {
        int index = 0;
        for (int i = 0; i < 26; i++) {
            if (column.matches(String.format("[%s]", Ex2Utils.ABC[i]))) {
                index = i;
            }
        }
        return index;
    }
}