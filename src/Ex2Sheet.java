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
        // Return formula as-is if it's a standalone numeric value, including negatives
        if (formula.matches("-?\\d+(\\.\\d+)?")) {
            return formula; // Example: returns "-9" or "9" as it is
        }

        StringBuilder resolvedFormula = new StringBuilder();
        String[] tokens = formula.split("(\\s+|(?=[+\\-*/()])|(?<=[+\\-*/()]))"); // Split into tokens

        for (String token : tokens) {
            token = token.trim();

            // Check for numeric literals (negative or positive)
            if (token.matches("-?\\d+(\\.\\d+)?")) {
                resolvedFormula.append(token).append(" ");
            } else if (token.matches("[A-Za-z]+\\d+")) { // Cell references (e.g., A1)
                // Resolve the reference
                int[] indices = CellReferenceResolver.resolveCellReference(token.toUpperCase());
                int refColumn = indices[0];
                int refRow = indices[1];

                String cellRef = CellEntry.toCellRef(refColumn, refRow);

                // Circular reference check
                if (cellRef.equals(currentCell) || visitedCells.contains(cellRef)) {
                    return Ex2Utils.ERR_CYCLE; // Circular dependency error
                }

                visitedCells.add(cellRef);
                SCell referencedCell = table[refColumn][refRow];
                String evaluatedValue = evaluateCellData(referencedCell, visitedCells, cellRef);
                visitedCells.remove(cellRef);

                if (Ex2Utils.ERR_CYCLE.equals(evaluatedValue) || Ex2Utils.ERR_FORM.equals(evaluatedValue)) {
                    return evaluatedValue;
                }

                resolvedFormula.append(evaluatedValue).append(" ");
            } else if (token.matches("[+*/()]")) { // Arithmetic operators other than '-'
                resolvedFormula.append(token).append(" ");
            } else if (token.equals("-")) { // Handle negative signs
                // Check if the last token is also '-'
                if (resolvedFormula.length() > 0 && resolvedFormula.toString().endsWith("- ")) {
                    // Simplify "--" to "+"
                    resolvedFormula.delete(resolvedFormula.length() - 2, resolvedFormula.length());
                    resolvedFormula.append("+ ");
                } else {
                    resolvedFormula.append(token).append(" ");
                }
            } else {
                return Ex2Utils.ERR_FORM; // Invalid token encountered
            }
        }

        // Cleanup redundant signs in the final expression
        String cleanedFormula = resolvedFormula.toString().trim()
                .replaceAll("\\-\\-+", "+")  // Simplify "--" to "+"
                .replaceAll("\\+-", "-")    // Simplify "+-" to "-"
                .replaceAll("-\\+", "-");   // Simplify "-+" to "-"

        return cleanedFormula;
    }

    private boolean isValidExpression(String formula) {
        // Regex to ensure the formula:
        // - Is composed of numbers, operators, and parentheses
        // - Numbers can be positive or negative (with optional decimals)
        // - Handles valid arithmetic structure with operators and operands
        // - Allows negative numbers after operators or at the start of the expression
        return formula.matches("-?\\d+(\\.\\d+)?([+\\-*/()]-?\\d+(\\.\\d+)?)*");
    }

    private double evalExpression(String expression) {
        // Preprocess the formula to normalize cases like =--9 or =- -9
        String cleanedExpression = preprocessExpression(expression);

        // Parse and evaluate the cleaned expression manually
        return evaluateCleanedExpression(cleanedExpression);
    }

    private String preprocessExpression(String expression) {
        // Remove all whitespace
        expression = expression.replaceAll("\\s+", "");

        // Replace consecutive double negatives (--9 → 9)
        while (expression.contains("--")) {
            expression = expression.replace("--", ""); // Replace redundant '--' with nothing
        }

        // Replace sequences of '- -' with '+'
        expression = expression.replace("- -", "+");

        // Replace '+-' with '-'
        expression = expression.replace("+-", "-");

        // Replace '-+' with '-'
        expression = expression.replace("-+", "-");

        return expression;
    }

    private double evaluateCleanedExpression(String expression) {
        // Edge case: handle empty or null expressions
        if (expression == null || expression.isEmpty()) {
            throw new IllegalArgumentException("Expression is null or empty.");
        }

        // Stack-based approach to parse and evaluate the mathematical expression
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        // Remove spaces and validate
        expression = expression.replaceAll("\\s+", "");

        // Edge case: If the expression starts with '-', handle as negative number
        if (expression.charAt(0) == '-') {
            expression = "0" + expression; // Convert `-9` to `0-9` to simplify parsing
        }

        int i = 0;
        while (i < expression.length()) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || c == '.') { // Parse numeric values (handles decimal points)
                StringBuilder sb = new StringBuilder();
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    sb.append(expression.charAt(i++));
                }
                values.push(Double.parseDouble(sb.toString()));
                continue;
            } else if (c == '(') {
                operators.push(c); // Push opening parenthesis
            } else if (c == ')') {
                // Process until matching opening parenthesis is found
                while (!operators.isEmpty() && operators.peek() != '(') {
                    values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                }
                if (!operators.isEmpty() && operators.peek() == '(') {
                    operators.pop(); // Remove the '('
                } else {
                    throw new IllegalArgumentException("Mismatched parentheses in the expression.");
                }
            } else if (isOperator(c)) {
                // Process operators while maintaining precedence rules
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(c)) {
                    values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                }
                operators.push(c); // Push the current operator
            } else {
                throw new IllegalArgumentException("Invalid character encountered: " + c);
            }
            i++;
        }

        // Process remaining operators
        while (!operators.isEmpty()) {
            if (operators.peek() == '(') {
                throw new IllegalArgumentException("Mismatched parentheses in the expression.");
            }
            values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
        }

        // The value stack should now contain exactly one result
        if (values.size() != 1) {
            throw new IllegalArgumentException("Invalid expression format.");
        }

        return values.pop();
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private int precedence(char op) {
        if (op == '+' || op == '-') return 1;
        if (op == '*' || op == '/') return 2;
        return 0;
    }

    private double applyOperator(char op, double b, double a) { // `a` is before `b`, as operations are applied in order
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/':
                if (b == 0) throw new ArithmeticException("Division by zero");
                return a / b;
        }
        throw new IllegalArgumentException("Invalid operator: " + op);
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