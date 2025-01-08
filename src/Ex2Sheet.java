import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private List<String> extractDependencies(SCell cell) {
        String formula = cell.getData(); // Fetch the formula (e.g., "=A1+B2", "=-A1+B2")
        List<String> dependencies = new ArrayList<>();

        // Regex to match valid cell references like A1, B2, but not unsupported negative references
        Matcher matcher = Pattern.compile("([A-Z]+\\d+)").matcher(formula);
        while (matcher.find()) {
            dependencies.add(matcher.group(1));
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
        return table.length > 0 ? table[0].length : 0;
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

            // Resolve references and simplify the formula
            String resolvedFormula = replaceReferencesInFormula(formula, visitedCells, currentCell);

            // If the resolved formula is invalid, return error
            if (resolvedFormula.isEmpty() || resolvedFormula.equals(Ex2Utils.ERR_FORM)) {
                return Ex2Utils.ERR_FORM;
            }

            // Check if the formula is valid
            if (!isValidExpression(resolvedFormula)) {
                return Ex2Utils.ERR_FORM;
            }

            // Evaluate and return the result
            try {
                double result = evalExpression(resolvedFormula);
                return Double.toString(result);
            } catch (IllegalArgumentException e) {
                return Ex2Utils.ERR_FORM;
            }
        }

        // If the data is a number, parse and return it
        if (SCell.isNumber(data)) {
            try {
                return String.valueOf(Double.parseDouble(data));
            } catch (NumberFormatException e) {
                return Ex2Utils.ERR_FORM;
            }
        }

        // If data is not a formula or a number, return as-is
        return data;
    }

    private String replaceReferencesInFormula(String formula, Set<String> visitedCells, String currentCell) {
        // Return formula as-is if standalone negative number
        if (formula.matches("-?\\d+(\\.\\d+)?")) {
            return formula; // Example: returns "-9" as it is
        }

        StringBuilder resolvedFormula = new StringBuilder();
        String[] tokens = formula.split("(\\s+|(?=[+\\-*/()])|(?<=[+\\-*/()]))"); // Split into tokens

        for (String token : tokens) {
            token = token.trim();

            // Check for numeric literals (negative or positive)
            if (token.matches("-?\\d+(\\.\\d+)?")) {
                resolvedFormula.append(token).append(" ");
            } else if (token.matches("[A-Za-z]+\\d+")) { // Cell references, e.g., A1
                // Resolve the reference
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
            } else if (token.matches("[+\\-*/()]")) { // Arithmetic operators
                resolvedFormula.append(token).append(" ");
            } else {
                return Ex2Utils.ERR_FORM; // Invalid token
            }
        }

        return resolvedFormula.toString().trim();
    }


    private boolean isValidExpression(String formula) {
        // Regex to match:
        // - An optional '=' at the beginning
        // - A number that can be positive or negative (with optional decimals)
        // - Arithmetic operators (+, -, *, /)
        // - Negative numbers after operators or at the start
        return formula.matches("=?-?\\d+(\\.\\d+)?([+\\-*/]-?\\d+(\\.\\d+)?)*");
    }

    private double evalExpression(String expression) {
        try {
            // Strip leading "=" if present
            if (expression.startsWith("=")) {
                expression = expression.substring(1).trim();
            }

            // Handle the evaluation using ExpressionEvaluator
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