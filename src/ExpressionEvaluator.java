import java.util.Stack;

public class ExpressionEvaluator {
    public double evaluate(String expression) throws Exception {
        // Remove all whitespace
        expression = expression.replaceAll("\\s", "");

        // Call the recursive helper function
        return evaluateExpression(expression);
    }

    private double evaluateExpression(String expression) throws Exception {
        Stack<Double> numbers = new Stack<>(); // Stack for numbers
        Stack<Character> operators = new Stack<>(); // Stack for operators

        int i = 0;
        while (i < expression.length()) {
            char currentChar = expression.charAt(i);

            // Case: Number
            if (Character.isDigit(currentChar) || currentChar == '.') {
                StringBuilder num = new StringBuilder();
                while (i < expression.length() &&
                        (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    num.append(expression.charAt(i));
                    i++;
                }
                numbers.push(Double.parseDouble(num.toString()));
                continue;
            }

            // Case: Opening parenthesis
            if (currentChar == '(') {
                operators.push(currentChar);
            }

            // Case: Closing parenthesis
            else if (currentChar == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
                }
                operators.pop();
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

        // Apply remaining operators
        while (!operators.isEmpty()) {
            numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
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
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/':
                if (b == 0) throw new Exception("Division by zero");
                return a / b;
        }
        throw new Exception("Invalid operator: " + operator);
    }
}