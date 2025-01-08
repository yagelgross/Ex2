import java.io.*;
import java.util.*;

public class Ex2Sheet implements Sheet {
    private final SCell[][] table;

    public Ex2Sheet(int width, int height) {
        this.table = new SCell[width][height];
    }

    /**
     * Extracts the dependencies from an SCell, assuming its data represents a formula.
     * E.g., if the formula is "=A1+B2", this method will extract and return ["A1", "B2"].
     *
     * @param cell The SCell from which dependencies should be extracted.
     * @return A list of dependencies as String references (e.g., "A1", "B2").
     */
    private List<String> extractDependencies(SCell cell) {
        if (cell == null || !SCell.isFormula(cell.getData())) return Collections.emptyList();
        String formula = cell.getData().substring(1); // Remove '='
        // Assume dependencies are separated by math operators (+,-,*,/)
        String[] tokens = formula.split("[^A-Za-z0-9]");
        List<String> dependencies = new ArrayList<>();
        for (String token : tokens) {
            if (token.matches("[A-Za-z]+\\d+")) { // Validate token as a cell reference (e.g., "A1")
                dependencies.add(token);
            }
        }
        return dependencies;
    }

    @Override
    public boolean isIn(int x, int y) {
        return x >= 0 && x < width() && y >= 0 && y < height();
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
    public void set(int x, int y, String c) {
        if (!isIn(x, y)) throw new IllegalArgumentException("Invalid cell coordinates!");
        if (table[x][y] == null) table[x][y] = new SCell(c);
        else table[x][y].setData(c); // Update cell content
    }

    @Override
    public Cell get(int x, int y) {
        return isIn(x, y) ? table[x][y] : null;
    }

    @Override
    public Cell get(String entry) {
        CellEntry cellEntry = new CellEntry(entry);
        return get(cellEntry.getX(), cellEntry.getY());
    }

    @Override
    public String value(int x, int y) {
        if (!isIn(x, y)) return null;
        SCell cell = table[x][y];
        if (cell == null) return null;
        return Integer.toString(Integer.parseInt(SCell.computeForm(cell.getData()))); // Evaluate cell
    }

    @Override
    public String eval(int x, int y) {
        return value(x, y); // Alias for value() in 1 cell
    }

    @Override
    public void eval() {
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                eval(x, y); // Evaluate all cells
            }
        }
    }

    @Override
    public int[][] depth() {
        int[][] depths = new int[width()][height()];
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                depths[x][y] = getDepth(x, y, new HashSet<>());
            }
        }
        return depths;
    }

    private int getDepth(int x, int y, Set<String> visited) {
        if (!isIn(x, y)) return 0;
        SCell cell = table[x][y];
        if (cell == null || !SCell.isFormula(cell.getData())) return 0;
        String cellRef = CellEntry.toCellRef(x, y);
        if (visited.contains(cellRef)) return -1; // Circular dependency
        visited.add(cellRef);
        int maxDepth = 0;
        for (String dep : extractDependencies(cell)) {
            CellEntry entry = new CellEntry(dep);
            maxDepth = Math.max(maxDepth, getDepth(entry.getX(), entry.getY(), visited));
        }
        return maxDepth + 1;
    }

    @Override
    public void save(String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("I2CS ArielU: SpreadSheet (Ex2) assignment - this line should be ignored\n");
            for (int x = 0; x < width(); x++) {
                for (int y = 0; y < height(); y++) {
                    SCell cell = table[x][y];
                    if (cell != null) {
                        writer.write(x + "," + y + "," + cell.getData() + "\n");
                    }
                }
            }
        }
    }

    @Override
    public void load(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            // Skip header
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length < 3) continue; // Ignore malformed lines
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                String content = parts[2];
                set(x, y, content);
            }
        }
    }

    @Override
    public void evaluate(int[][] dd) {
        for (int depth = 0; depth <= getMaxDepth(dd); depth++) {
            for (int x = 0; x < width(); x++) {
                for (int y = 0; y < height(); y++) {
                    if (dd[x][y] == depth) {
                        eval(x, y); // Evaluate the cell at the current depth
                    }
                }
            }
        }
    }

    private int getMaxDepth(int[][] dd) {
        int maxDepth = 0;
        for (int x = 0; x < dd.length; x++) {
            for (int y = 0; y < dd[x].length; y++) {
                if (dd[x][y] > maxDepth) {
                    maxDepth = dd[x][y];
                }
            }
        }
        return maxDepth;
    }
}