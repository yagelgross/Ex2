import java.util.Stack;

public class ExpressionEvaluator {
    public double evaluate(String expression) throws Exception {
        // Remove all whitespace from the input
        expression = expression.replaceAll("\\s", "");

        // Evaluate the sanitized expression
        return evaluateExpression(expression);
    }

    private double evaluateExpression(String expression) throws Exception {
        Stack<Double> numbers = new Stack<>(); // Stack to hold numbers
        Stack<Character> operators = new Stack<>(); // Stack to hold operators

        int i = 0;
        while (i < expression.length()) {
            char currentChar = expression.charAt(i);

            // Case: Number or Negative Number
            if (Character.isDigit(currentChar) || currentChar == '.' ||
                    (currentChar == '-' && (i == 0 || expression.charAt(i - 1) == '(' || isOperator(expression.charAt(i - 1))))) {
                StringBuilder num = new StringBuilder();
                // Check for a sequence of consecutive '-' signs
                if (currentChar == '-') {
                    int signCount = 0;

                    // Count consecutive '-' signs
                    while (i < expression.length() && expression.charAt(i) == '-') {
                        signCount++;
                        i++;
                    }

                    // Determine the effective sign: odd -> negative, even -> positive
                    if (signCount % 2 != 0) {
                        num.append('-');
                    }
                }

                // Extract the rest of the number
                while (i < expression.length() &&
                        (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    num.append(expression.charAt(i));
                    i++;
                }

                numbers.push(Double.parseDouble(num.toString()));
                continue;
            }

            // Case: Opening Parenthesis
            if (currentChar == '(') {
                operators.push(currentChar);
            }

            // Case: Closing Parenthesis
            else if (currentChar == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
                }
                if (operators.isEmpty()) {
                    throw new Exception("Mismatched parentheses");
                }
                operators.pop(); // Remove the '('
            }

            // Case: Operator (+, -, *, /)
            else if (isOperator(currentChar)) {
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(currentChar)) {
                    numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
                }
                operators.push(currentChar);
            }

            i++;
        }

        // Process any remaining operators
        while (!operators.isEmpty()) {
            numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
        }

        if (numbers.size() != 1) {
            throw new Exception("Invalid expression");
        }

        return numbers.pop();
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private int precedence(char operator) {
        if (operator == '+' || operator == '-') return 1;
        if (operator == '*' || operator == '/') return 2;
        return -1;
    }

    private double applyOperation(char operator, double b, double a) throws Exception {
        switch (operator) {
            case '+' -> {
                return a + b;
            }
            case '-' -> {
                return a - b;
            }
            case '*' -> {
                return a * b;
            }
            case '/' -> {
                if (b == 0) throw new Exception("Division by zero");
                return a / b;
            }
        }
        throw new Exception("Invalid operator");
    }
}