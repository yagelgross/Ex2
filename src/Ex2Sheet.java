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

    private String evaluateReferencedCellData(SCell cell, Set<String> visited) {
        String data = cell.getData();

        if (data == null || data.isBlank()) {
            return ""; // Empty cell
        }

        if (!SCell.isFormula(data) && !SCell.isNumber(data)) {
            return data;
        }

        if (SCell.isFormula(data)) {
            String formula = data.substring(1).trim(); // Remove '='

            // Resolve references
            String resolvedFormula = replaceReferencesInFormula(formula, visited, cell.getData());

            if ("ERR".equals(resolvedFormula)) {
                return Ex2Utils.ERR_FORM; // Invalid reference
            }

            try {
                double result = evalExpression(resolvedFormula); // Custom evaluator
                return Double.toString(result);
            } catch (IllegalArgumentException e) {
                System.err.println("Error in formula evaluation: " + e.getMessage());
                return Ex2Utils.ERR_FORM; // Error in arithmetic
            }
        }

        // Handle raw numeric data or plain text
        try {
            return Double.toString(Double.parseDouble(data));
        } catch (NumberFormatException e) {
            return data; // Return plain text as is
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
        return evaluateCellData(cell, new HashSet<>(), CellEntry.toCellRef(x, y));
    }

    /**
     * Evaluates the data of a given cell and converts it into a string.
     * Performs validation to avoid invalid parsing.
     */
    private String evaluateCellData(SCell cell, Set<String> visitedCells, String currentCell) {
        String data = cell.getData().trim(); // Get cell data, and trim whitespace

        if (SCell.isFormula(data)) {
            // If the data is a formula (starts with '='), process it as a formula
            String formula = data.substring(1).trim(); // Strip '=' from the formula
            String resolvedFormula = replaceReferencesInFormula(formula, visitedCells, currentCell);

            // If errors are detected during reference replacement, propagate them
            if (Ex2Utils.ERR_CYCLE.equals(resolvedFormula)) {
                return Ex2Utils.ERR_CYCLE;
            }
            if (Ex2Utils.ERR_FORM.equals(resolvedFormula)) {
                return Ex2Utils.ERR_FORM;
            }

            // Validate formula before passing to evaluation
            if (!isValidExpression(resolvedFormula)) {
                System.err.println("Invalid formula: " + resolvedFormula);
                return Ex2Utils.ERR_FORM;
            }

            try {
                double result = evalExpression(resolvedFormula);
                return Double.toString(result);
            } catch (IllegalArgumentException e) {
                System.err.println("Failed to evaluate formula: " + resolvedFormula);
                return Ex2Utils.ERR_FORM;
            }
        } else {
            // If the data is not a formula (doesn't start with '='), process it as a plain value or text
            try {
                // Try to parse the input as a double
                double numericValue = Double.parseDouble(data);
                return Double.toString(numericValue); // If successful, return it as a number
            } catch (NumberFormatException e) {
                // If parsing fails, treat the input as plain text
                return data;
            }
        }
    }

    private String replaceReferencesInFormula(String formula, Set<String> visitedCells, String currentCell) {
        StringBuilder resolvedFormula = new StringBuilder();

        // Split the formula into valid components (tokens)
        String[] tokens = formula.split("(\\s+|(?=[+\\-*/()])|(?<=[+\\-*/()]))");
        for (String token : tokens) {
            token = token.trim();

            // Check if the token is a valid cell reference (e.g., A1 or a1)
            if (token.matches("[A-Za-z]+\\d+")) {
                // Normalize token to uppercase to resolve case sensitivity
                String normalizedToken = token.toUpperCase();

                // Resolve cell reference to column and row
                int[] indices = CellReferenceResolver.resolveCellReference(normalizedToken);
                int refColumn = indices[0];
                int refRow = indices[1];

                // Detect direct circular reference
                String cellRef = CellEntry.toCellRef(refColumn, refRow);
                if (cellRef.equals(currentCell)) {
                    System.err.println("Direct circular reference detected: " + currentCell);
                    return Ex2Utils.ERR_CYCLE; // Return a circular error
                }

                // Detect indirect circular dependency
                if (visitedCells.contains(cellRef)) {
                    System.err.println("Circular dependency detected: " + currentCell + " -> " + cellRef);
                    return Ex2Utils.ERR_CYCLE;
                }

                // Add the cell reference to visited cells
                visitedCells.add(cellRef);

                // Evaluate the referenced cell
                SCell referencedCell = table[refColumn][refRow];
                String evaluatedValue = evaluateCellData(referencedCell, visitedCells, cellRef);

                // Backtrack (remove from visited cells)
                visitedCells.remove(cellRef);

                // Propagate any errors
                if (Ex2Utils.ERR_CYCLE.equals(evaluatedValue) || Ex2Utils.ERR_FORM.equals(evaluatedValue)) {
                    return evaluatedValue;
                }

                // Append the resolved value to the formula
                resolvedFormula.append(evaluatedValue).append(" ");
            } else {
                // Append constants and operators as-is
                resolvedFormula.append(token).append(" ");
            }
        }

        return resolvedFormula.toString().trim();
    }


    private boolean isValidExpression(String formula) {
        // Allow numbers, operators (+, -, *, /), parentheses, spaces, and decimals
        return formula.matches("[0-9+\\-*/().\\s]*");
    }

    private double evalExpression(String expression) {
        try {
            return new ExpressionEvaluator().evaluate(expression);
        } catch (Exception e) {
            System.err.println("Failed to evaluate expression: " + expression);
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