import java.util.*;
import java.util.regex.Pattern;

public class SCell implements Cell {
    private String line; // The raw content of the cell
    private int type;    // The type of the cell (e.g., TEXT, NUMBER, FORMULA)
    private int order;   // Computation order of the cell
    private String evaluated; // Evaluated value of the cell (after formula resolution)

    // Dependency management
    private final Set<SCell> dependents = new HashSet<>();
    private final Map<String, SCell> dependencies = new HashMap<>();

    /**
     * Constructor - initializes the raw data of the cell.
     */
    public SCell(String rawLine) {
        setData(rawLine);
        this.type = Ex2Utils.TEXT;
        this.order = 0;
    }

    /**
     * Constructor with both raw and evaluated values.
     */
    public SCell(String original, String evaluated) {
        this.line = original;
        this.evaluated = evaluated;
        this.type = isFormula(original) ? Ex2Utils.FORM : Ex2Utils.TEXT;
    }

    // ---------------- Core Utility Functions ----------------

    /**
     * Determines if the given string is a valid formula.
     */
    public static boolean isFormula(String input) {
        if (input == null || !input.startsWith("=")) {
            return false; // Not a formula if no "=" at start
        }

        // Strip the leading '=' and validate expression
        String formulaBody = input.substring(1).trim();
        return formulaBody.matches("-?\\d+(\\.\\d+)?([+\\-*/]-?\\d+(\\.\\d+)?)*");
    }

    /**
     * Determines whether the given string is numeric.
     */
    public static boolean isNumber(String strNum) {
        if (strNum == null) return false;
        return Pattern.matches("-?\\d+(\\.\\d+)?", strNum);
    }
    /**
     * Computes the result of a formula.
     */
    public static String computeForm(String formula) {
        if (!isFormula(formula)) {
            return Ex2Utils.ERR_FORM; // Error for invalid formula
        }
        try {
            String expression = formula.substring(1); // Remove the '=' character
            double result = eval(expression);
            return String.valueOf(result);
        } catch (Exception e) {
            return Ex2Utils.ERR_FORM; // Error during evaluation
        }
    }

    private static double eval(String expression) {
        char[] chars = expression.toCharArray();
        return parseExpression(chars, new int[]{0});
    }

    private static double parseExpression(char[] chars, int[] index) {
        double result = parseTerm(chars, index);
        while (index[0] < chars.length) {
            char operator = chars[index[0]];
            if (operator == '+' || operator == '-') {
                index[0]++;
                double term = parseTerm(chars, index);
                result = operator == '+' ? result + term : result - term;
            } else break;
        }
        return result;
    }

    private static double parseTerm(char[] chars, int[] index) {
        double result = parseFactor(chars, index);
        while (index[0] < chars.length) {
            char operator = chars[index[0]];
            if (operator == '*' || operator == '/') {
                index[0]++;
                double factor = parseFactor(chars, index);
                result = operator == '*' ? result * factor : result / factor;
            } else break;
        }
        return result;
    }

    private static double parseFactor(char[] chars, int[] index) {
        if (chars[index[0]] == '(') {
            index[0]++;
            double result = parseExpression(chars, index);
            index[0]++; // Consume the closing ')'
            return result;
        }
        int start = index[0];
        while (index[0] < chars.length && (Character.isDigit(chars[index[0]]) || chars[index[0]] == '.')) {
            index[0]++;
        }
        return Double.parseDouble(new String(chars, start, index[0] - start));
    }

    // ------------- Handling Cell Evaluation & Dependencies -------------

    /**
     * Clears all dependencies for reevaluation.
     */
    public void clearDependencies() {
        for (SCell dependency : dependencies.values()) {
            dependency.dependents.remove(this);
        }
        dependencies.clear();
    }

    /**
     * Adds a dependency on another cell.
     */
    private void addDependency(String refName, SCell referencedCell) {
        dependencies.put(refName, referencedCell);
        referencedCell.dependents.add(this);
    }

    /**
     * Updates the raw content of the cell.
     */
    @Override
    public void setData(String content) {
        this.line = content;

        if (!isFormula(content)) {
            this.evaluated = content; // Plain content
            clearDependencies(); // Remove dependencies if not a formula
        } else {
            this.evaluated = null; // Needs evaluation
        }
    }

    /**
     * Evaluates the content of the cell, resolving formulas and references.
     */
    public void evaluate(Map<String, SCell> cellMap) {
        if (!isFormula(line)) {
            this.evaluated = line; // Non-formula content is just raw content
            return;
        }

        try {
            clearDependencies(); // Reset dependencies before evaluation
            String formula = line.substring(1); // Remove '='
            String[] tokens = formula.split("\\s+"); // Split tokens by spaces

            double result = 0.0;
            String operator = "+";
            boolean expectingOperand = true;

            for (String token : tokens) {
                if (token.matches("[-+*/()]")) { // Operators or parentheses
                    operator = token;
                    expectingOperand = true;
                } else if (cellMap.containsKey(token)) { // Cell reference
                    SCell referencedCell = cellMap.get(token);
                    addDependency(token, referencedCell); // Track dependency
                    double referencedValue = Double.parseDouble(referencedCell.getEvaluated());
                    result = calculate(result, referencedValue, operator);
                    expectingOperand = false;
                } else if (isNumber(token)) { // Numeric literals
                    double value = Double.parseDouble(token);
                    result = calculate(result, value, operator);
                    expectingOperand = false;
                } else {
                    throw new IllegalArgumentException("Invalid token in formula: " + token);
                }
            }

            if (expectingOperand) {
                throw new IllegalArgumentException("Malformed formula: Missing operand.");
            }

            this.evaluated = String.valueOf(result); // Save computed value
        } catch (Exception e) {
            this.evaluated = Ex2Utils.ERR_FORM; // Evaluation error
        }

        // Notify dependents for reevaluation
        for (SCell dependent : dependents) {
            dependent.evaluate(cellMap);
        }
    }

    private double calculate(double left, double right, String operator) {
        return switch (operator) {
            case "+" -> left + right;
            case "-" -> left - right;
            case "*" -> left * right;
            case "/" -> left / right;
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        };
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String getData() {
        return line;
    }
    @Override
    public int getType() {
        return getType(this.line);
    }
    public void setType(int type, String cellData) {
        this.type = type;
        if (type == Ex2Utils.FORM) {
            this.evaluated = computeForm(cellData);
        }
    }
    public int getType(String cellData) {
        if (cellData == null || cellData.trim().isEmpty()) {
            return 0; // Empty cell
        }
        if (cellData.equals(Ex2Utils.ERR_FORM)) {
            return Ex2Utils.ERR; // Invalid formula
        }
        if (cellData.startsWith("=")) {
            // Extract the expression and trim it
            String formula = cellData.substring(1).trim();

            // If valid formula (including standalone negative numbers like `=-9`), return FORM
            if (isValidFormula(formula) || isNumeric(formula)) {
                return Ex2Utils.FORM;
            }
            return Ex2Utils.ERR; // Invalid formula
        }
        if (isNumeric(cellData)) {
            return Ex2Utils.NUMBER; // Numeric value
        }
        if (cellData.startsWith("'")) {
            return Ex2Utils.TEXT; // Explicit text (e.g., "'Hello")
        }
        return Ex2Utils.TEXT; // Default to text
    }

    private boolean isNumeric(String data) {
        try {
            Double.parseDouble(data);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidFormula(String formula) {
        // Example regex for mathematical expressions like -12, 23+6, or (A1+B2)
        return formula.matches("[A-Za-z0-9\\+\\-\\*/\\(\\)\\.\\s]+"); // Regex to allow valid operations
    }

    @Override
    public void setType(int type) {
        this.type = type;
    }

    public String getEvaluated() {
        return evaluated;
    }

    @Override
    public String toString() {
        return getEvaluated();
    }
    public String getOriginal() {
        return line;
    }
}