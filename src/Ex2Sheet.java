import java.io.*;
import java.util.*;

public class Ex2Sheet implements Sheet {
    private final SCell[][] table;

    public Ex2Sheet(int width, int height) {
        table = new SCell[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                table[i][j] = new SCell(""); // Initialize empty cells
            }
        }
    }

    /**
     * Extracts the dependencies from an SCell, assuming its data represents a formula.
     * E.g., if the formula is "=A1+B2", this method will extract and return ["A1", "B2"].
     *
     * @param cell The SCell from which dependencies should be extracted.
     * @return A list of dependencies as String references (e.g., "A1", "B2").
     */
    protected List<String> extractDependencies(SCell cell) {
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < height(); row++) {
            for (int col = 0; col < width(); col++) {
                SCell cell = (SCell) get(col, row);
                String value = (cell == null || cell.getEvaluated() == null) ? " " : cell.getEvaluated();
                builder.append(value).append("\t");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    @Override
    public boolean isIn(int col, int row) {
        return col >= 0 && col < 26 && row >= 0 && row < 100;
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
        if (isIn(x, y)) table[x][y] = new SCell(c);
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
        return evaluateCellData(cell, new HashSet<>(), CellEntry.toCellRef(x, y));
    }

    /**
     * Evaluates the data of a given cell and converts it into a string.
     * Performs validation to avoid invalid parsing.
     */
    private String evaluateCellData(SCell cell, Set<String> visitedCells, String currentCell) {
        String data = cell.getData();

        // Check if the cell data starts with "="
        if (data.startsWith("=")) {
            // Remove the leading "=" for processing
            String formula = data.substring(1).trim();

            // Resolve references and validate the formula
            String resolvedFormula = replaceReferencesInFormula(formula, visitedCells, currentCell);

            // Check invalid or unresolved formulas
            if (Ex2Utils.ERR_FORM.equals(resolvedFormula) || Ex2Utils.ERR_CYCLE.equals(resolvedFormula)) {
                return resolvedFormula; // Propagate the error
            }

            // Final validation of resolved formula
            if (!isValidExpression(resolvedFormula)) {
                return Ex2Utils.ERR_FORM;
            }

            // Try to evaluate the valid formula
            try {
                double result = evalExpression(resolvedFormula);
                return Double.toString(result);
            } catch (IllegalArgumentException e) {
                return Ex2Utils.ERR_FORM; // Handle arithmetic evaluation errors
            }
        }
        if (SCell.isNumber(data)) {
            return String.valueOf(Double.parseDouble(data));
        }

        // If data is not a formula, return as-is
        return data;
    }

    private String replaceReferencesInFormula(String formula, Set<String> visitedCells, String currentCell) {
        StringBuilder resolvedFormula = new StringBuilder();

        // Tokenize the formula into manageable parts
        String[] tokens = formula.split("(\\s+|(?=[+\\-*/()])|(?<=[+\\-*/()]))");
        for (String token : tokens) {
            token = token.trim();

            // Check for valid cell references
            if (token.matches("[A-Za-z]+\\d+")) {
                int[] indices = CellReferenceResolver.resolveCellReference(token.toUpperCase());
                int refColumn = indices[0];
                int refRow = indices[1];

                String cellRef = CellEntry.toCellRef(refColumn, refRow);

                // Circular reference check
                if (cellRef.equals(currentCell) || visitedCells.contains(cellRef)) {
                    return Ex2Utils.ERR_CYCLE;
                }

                visitedCells.add(cellRef);
                SCell referencedCell = table[refColumn][refRow];
                String evaluatedValue = evaluateCellData(referencedCell, visitedCells, cellRef);
                visitedCells.remove(cellRef);

                if (Ex2Utils.ERR_CYCLE.equals(evaluatedValue) || Ex2Utils.ERR_FORM.equals(evaluatedValue)) {
                    return evaluatedValue;
                }

                resolvedFormula.append(evaluatedValue).append(" ");
            }
            // Flag invalid tokens (e.g., standalone text like "bus")
            else if (token.matches("[A-Za-z]+")) {
                return Ex2Utils.ERR_FORM;
            }
            // Append valid operators and constants
            else {
                resolvedFormula.append(token).append(" ");
            }
        }
        return resolvedFormula.toString().trim();
    }


    private boolean isValidExpression(String formula) {
        // Allow only numbers, operators, parentheses, and valid cell references
        // Disallow pure strings like "bus"
        return formula.matches("([A-Za-z]+\\d+|[0-9+\\-*/().\\s])*");
    }

    private double evalExpression(String expression) {
        try {
            return new ExpressionEvaluator().evaluate(expression);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to evaluate expression: " + expression, e);
        }
    }

    @Override
    public String eval(int x, int y) {
        return value(x, y); // Alias for value() in 1 cell
    }

    @Override
    public void eval() {
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                SCell cell = table[x][y];
                if (cell != null) {
                    cell.evaluate(null /* Pass a map of dependencies if available */);
                }
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
            writer.write("I2CS ArielU: SpreadSheet (Ex2) assignment\n");
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
        for (int[] ints : dd) {
            for (int anInt : ints) {
                if (anInt > maxDepth) {
                    maxDepth = anInt;
                }
            }
        }
        return maxDepth;
    }

}