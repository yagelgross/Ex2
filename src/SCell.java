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
     * Default Constructor initializes the cell's raw content.
     * Sets a default type (TEXT) and order (0).
     */
    public SCell(String rawLine) {
        setData(rawLine);
        this.type = Ex2Utils.TEXT;
        this.order = 0;
    }

    /**
     * Constructor with both raw and evaluated values defined.
     */
    public SCell(String original, String evaluated) {
        this.line = original;
        this.evaluated = evaluated;
        this.type = isFormula(original) ? Ex2Utils.FORM : Ex2Utils.TEXT;
    }

    // ---------------- Core Utility Functions ----------------

    /**
     * Determines if the input is a valid formula.
     */
    public static boolean isFormula(String input) {
        boolean isValid = false;
        final String formulaRegex = "^=(([a-zA-Z]\\d+)|\\d+(\\.\\d+)?|\\(([^()]+|[^()]*\\([^()]+\\))*\\))"
                + "(\\s*[+\\-*/]\\s*(([a-zA-Z]\\d+)|\\d+(\\.\\d+)?|\\(([^()]+|[^()]*\\([^()]+\\))*\\)))*$";
        if (Pattern.matches(formulaRegex, input)) {
            isValid = true;
        }
        if (input.matches(".*[a-zA-Z]{2,}.*")) { // Invalid if it contains multiple letters in a row
            isValid = false;
        }
        return isValid;
    }

    /**
     * Determines if the content is either a number or formula.
     */
    public static boolean isForm(String str) {
        return isFormula(str) || isNumber(str);
    }

    /**
     * Determines if the content is plain text.
     */
    public static boolean isText(String str) {
        return !isFormula(str) && !isNumber(str);
    }

    /**
     * Determines whether the given string is a numeric value.
     */
    public static boolean isNumber(String strNum) {
        if (strNum == null || strNum.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Computes the result of a valid formula.
     * If invalid, it returns an error.
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

    // ------------- Cell Content Management & Evaluation -------------

    /**
     * Clears all dependencies before re-evaluation.
     */
    private void clearDependencies() {
        for (SCell dependency : dependencies.values()) {
            dependency.dependents.remove(this);
        }
        dependencies.clear();
    }

    /**
     * Adds a dependency on a referenced cell.
     */
    private void addDependency(String refName, SCell referencedCell) {
        dependencies.put(refName, referencedCell);
        referencedCell.dependents.add(this);
    }

    /**
     * Sets the raw data of this cell and clears dependencies if it's no longer a formula.
     */
    @Override
    public void setData(String content) {
        this.line = content;

        if (!isFormula(content)) {
            this.evaluated = content; // Simple cell value
            clearDependencies(); // Not a formula, so no dependencies
        } else {
            this.evaluated = null; // Reset evaluated value
        }
    }

    /**
     * Evaluates the cell value by processing its content or referencing dependencies.
     */
    public void evaluate(Map<String, SCell> cellMap) {
        if (!isFormula(line)) {
            this.evaluated = line; // Raw content for non-formula cells
            return;
        }

        try {
            clearDependencies(); // Clear old dependencies
            String formula = line.substring(1); // Remove '=' character
            String[] tokens = formula.split("\\s+");

            double result = 0.0;
            String operator = "+";
            boolean expectingOperand = true;

            for (String token : tokens) {
                if (token.matches("[-+*/()]")) { // Operators or parentheses
                    operator = token;
                    expectingOperand = true;
                } else if (cellMap.containsKey(token)) { // Cell references
                    SCell referencedCell = cellMap.get(token);
                    addDependency(token, referencedCell); // Add dependency
                    double referencedValue = Double.parseDouble(referencedCell.getEvaluated());
                    result = calculate(result, referencedValue, operator);
                    expectingOperand = false;
                } else if (isNumber(token)) { // Numeric values
                    double value = Double.parseDouble(token);
                    result = calculate(result, value, operator);
                    expectingOperand = false;
                } else {
                    throw new IllegalArgumentException("Invalid token in formula: " + token);
                }
            }

            if (expectingOperand) {
                throw new IllegalArgumentException("Malformed formula: Missing an operand.");
            }

            this.evaluated = String.valueOf(result);
        } catch (Exception e) {
            this.evaluated = Ex2Utils.ERR_FORM; // Error case
        }

        // Notify all dependent cells
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

    // ----------- Interface Method Implementations ------------

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
        return type;
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
}