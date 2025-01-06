// Add your documentation below:

// Add your documentation below:

import java.util.regex.Pattern;


public class SCell implements Cell {
    private String line; // The content of the cell
    private int type;    // The type of the cell (e.g., TEXT, NUMBER, etc.)
    private int order;   // The natural order of the cell for computation

    /**
     * Constructor initializes the `line` with the given string
     * and sets a default type and order.
     */
    public SCell(String s) {
        setData(s);         // Set the data for the cell
        this.type = Ex2Utils.TEXT; // Default type is TEXT
        this.order = 0;     // Default computation order is 0
    }

    /**
     * Returns the computation order of the cell.
     * The default implementation assumes it's 0.
     */
    @Override
    public int getOrder() {
        return this.order; // Return the current computation order
    }

    /**
     * Sets the order of the cell to the given value.
     */
    @Override
    public void setOrder(int t) {
        this.order = t; // Assign given value as the new order
    }

    /**
     * Overrides `toString` to return the raw data content.
     */
    @Override
    public String toString() {
        return getData();
    }

    /**
     * Sets the content of the cell.
     */
    @Override
    public void setData(String s) {
        this.line = s;      // Update the cell's data
        // Assuming basic logic to determine type based on input
        if (s.isEmpty()) {
            this.type = Ex2Utils.TEXT; // Empty content is treated as text
        } else if (s.matches("-?\\d+(\\.\\d+)?")) { // Matches numeric content
            this.type = Ex2Utils.NUMBER;
        } else if (s.startsWith("=")) { // Starts with `=` suggests formula
            this.type = Ex2Utils.FORM;
        } else {
            this.type = Ex2Utils.TEXT; // Default to TEXT for anything else
        }
    }

    /**
     * Gets the content of the cell.
     */
    @Override
    public String getData() {
        return this.line; // Return the raw data content
    }

    /**
     * Gets the type of the cell (TEXT, NUMBER, FORM, etc.).
     */
    @Override
    public int getType() {
        return this.type; // Return the current type
    }

    /**
     * Sets the type of the cell explicitly.
     */
    @Override
    public void setType(int t) {
        this.type = t; // Assign given type value
    }
    public static boolean isNumber(String strNum) {
        if (strNum == null || strNum.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(strNum); // Additional fallback for parsing
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static boolean isFormula(String input) {
        boolean isValid = false;
        // Regular expression for validating the formula
        final String formulaRegex = "^=(([a-zA-Z]\\d+)|\\d+(\\.\\d+)?|\\(([^()]" +
                "+|[^()]*\\([^()]+\\))*\\))(\\s*[+\\-*/]\\s*" +
                "(([a-zA-Z]\\d+)|\\d+(\\.\\d+)?|\\(([^()]+|" +
                "[^()]*\\([^()]+\\))*\\)))*$";

        // Use the regex pattern to match the input
        if (Pattern.matches(formulaRegex, input)) {
            isValid = true;
        }
        // If it contains more than one letter in a row
        if (input.matches(".*[a-zA-Z]{2,}.*")) {
            isValid = false;
        }
        return isValid;
    }
    public static boolean isForm(String str) {
        return isFormula(str) || isNumber(str);
    }
    public static boolean isText(String str) {
        return !isFormula(str) && !isNumber(str);
    }
    public static boolean isErr(String str) {
        return str.equals(Ex2Utils.ERR_FORM) || str.equals(Ex2Utils.ERR_CYCLE);
    }
    
    /**
     * Computes the result of a formula (e.g., "=3+5").
     * If the formula is invalid, returns a specific error message.
     */
    public static String computeForm(String formula) {
        if (!isFormula(formula)) {
            return Ex2Utils.ERR_FORM; // Return error for invalid formula
        }
    
        try {
            String expression = formula.substring(1); // Remove '=' from the formula
            // Call a utility (hypothetical) to evaluate the formula
            double result = eval(expression);
            return String.valueOf(result);
        } catch (Exception e) {
            return Ex2Utils.ERR_FORM; // Return error on any exception
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
            index[0]++; // Consume ')'
            return result;
        }
        int start = index[0];
        while (index[0] < chars.length && (Character.isDigit(chars[index[0]]) || chars[index[0]] == '.')) {
            index[0]++;
        }
        return Double.parseDouble(new String(chars, start, index[0] - start));
    }

}