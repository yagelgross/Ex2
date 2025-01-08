import java.io.*;
import java.util.*;

public class Ex2Sheet implements Sheet {
    private final SCell[][] table;
    private String[][] ORIGINAL;

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
        if (isIn(x, y)) {
            table[x][y] = new SCell(c);
        }
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
        return evaluateCellData(cell);
    }

    /**
     * Evaluates the data of a given cell and converts it into a string.
     * Performs validation to avoid invalid parsing.
     */
    private String evaluateCellData(SCell cell) {
        String data = cell.getData();

        if (data == null || data.isBlank()) {
            return ""; // Empty cell
        }

        if (SCell.isFormula(data)) {
            String formula = data.substring(1).trim(); // Remove '='
            System.out.println("Processing formula: " + formula);

            // Resolve references
            String resolvedFormula = replaceReferencesInFormula(formula);

            if ("ERR".equals(resolvedFormula)) {
                return "ERR_FORM"; // Invalid reference
            }

            try {
                double result = evalExpression(resolvedFormula); // Custom evaluator
                return Double.toString(result);
            } catch (IllegalArgumentException e) {
                System.err.println("Error in formula evaluation: " + e.getMessage());
                return "ERR_FORM"; // Error in arithmetic
            }
        }

        // Handle raw numeric data or plain text
        try {
            return Double.toString(Double.parseDouble(data));
        } catch (NumberFormatException e) {
            return data; // Return plain text as is
        }
    }

    private String replaceReferencesInFormula(String formula) {
        StringBuilder resolvedFormula = new StringBuilder();

        // Debug: Log the initial formula
        System.out.println("Resolving formula references: " + formula);

        // Split the formula into tokens (e.g., "A1 + B1")
        String[] tokens = formula.split(" ");
        for (String token : tokens) {
            try {
                // If the token matches a possible reference, resolve it
                if (token.matches("[A-Za-z]+\\d+")) {
                    int[] indices = CellReferenceResolver.resolveCellReference(token); // Leverage CellReferenceResolver
                    int refRow = indices[0];
                    int refColumn = indices[1];

                    // Debug: Log the referenced cell's resolved position
                    System.out.println("Found reference: " + token + " -> (Row: " + refRow + ", Column: " + refColumn + ")");

                    // Ensure the reference is valid and within bounds
                    if (!isIn(refColumn, refRow)) {
                        return "ERR"; // Invalid reference
                    }

                    // Get the referenced cell and evaluate it
                    SCell referencedCell = table[refColumn][refRow];
                    String referencedValue = evaluateCellData(referencedCell);

                    // Propagate error if the referenced cell is invalid
                    if ("ERR_FORM".equals(referencedValue)) {
                        return "ERR";
                    }

                    resolvedFormula.append(referencedValue).append(" ");
                } else {
                    // Append operators and constants as-is
                    resolvedFormula.append(token).append(" ");
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid reference format: " + token);
                return "ERR";
            }
        }

        // Debug: Log the final resolved formula
        System.out.println("Final resolved formula: " + resolvedFormula.toString().trim());

        return resolvedFormula.toString().trim();
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


    private double evaluate(String expr) {
        // This stack-based implementation uses the Shunting Yard algorithm for correct precedence
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        int i = 0;
        while (i < expr.length()) {
            char currentChar = expr.charAt(i);

            // Case 1: If current token is a number, parse the full number
            if (Character.isDigit(currentChar) || currentChar == '.') {
                int start = i;
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    i++;
                }
                double value = Double.parseDouble(expr.substring(start, i));
                values.push(value);
                continue; // Skip to the next token
            }

            // Case 2: If current token is an operator
            if (isOperator(currentChar)) {
                // Handle operator precedence: Process any higher precedence operators first
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(currentChar)) {
                    double b = values.pop();
                    double a = values.pop();
                    char op = operators.pop();
                    values.push(applyOperator(a, b, op));
                }

                // Push the current operator to the stack
                operators.push(currentChar);
            }

            // Move to the next character
            i++;
        }

        // After traversing the expression, process remaining operators
        while (!operators.isEmpty()) {
            double b = values.pop();
            double a = values.pop();
            char op = operators.pop();
            values.push(applyOperator(a, b, op));
        }

        // The final result is the last value remaining in the stack
        return values.pop();
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private int precedence(char operator) {
        if (operator == '+' || operator == '-') {
            return 1; // Lowest precedence
        } else if (operator == '*' || operator == '/') {
            return 2; // Higher precedence
        }
        return -1;  // Invalid operator
    }

    private double applyOperator(double a, double b, char operator) {
        switch (operator) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return a / b;
            default:
                throw new IllegalArgumentException("Unexpected operator: " + operator);
        }
    }
}