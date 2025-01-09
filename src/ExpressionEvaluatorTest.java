import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExpressionEvaluatorTest {

    /**
     * Tests for the evaluate method in ExpressionEvaluator class.
     * <p>
     * The evaluate(String expression) method evaluates a mathematical expression represented as
     * a string, handling basic arithmetic operations (+, -, *, /), parentheses, and numbers.
     * The method throws an Exception for invalid expressions.
     */

    @Test
    public void testEvaluateSimpleAddition() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        String expression = "2+3";
        double result = evaluator.evaluate(expression);
        assertEquals(5.0, result, 0.001);
    }

    @Test
    public void testEvaluateSimpleSubtraction() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        String expression = "10-7";
        double result = evaluator.evaluate(expression);
        assertEquals(3.0, result, 0.001);
    }

    @Test
    public void testEvaluateSimpleMultiplication() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        String expression = "4*5";
        double result = evaluator.evaluate(expression);
        assertEquals(20.0, result, 0.001);
    }

    @Test
    public void testEvaluateSimpleDivision() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        String expression = "20/4";
        double result = evaluator.evaluate(expression);
        assertEquals(5.0, result, 0.001);
    }

    @Test
    public void testEvaluateDivisionWithDecimals() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        String expression = "7/3";
        double result = evaluator.evaluate(expression);
        assertEquals(2.333, result, 0.001);
    }

    @Test
    public void testEvaluateWithParentheses() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        String expression = "(2+3)*4";
        double result = evaluator.evaluate(expression);
        assertEquals(20.0, result, 0.001);
    }

    @Test
    public void testEvaluateWithNestedParentheses() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        String expression = "((1+2)*3)-4";
        double result = evaluator.evaluate(expression);
        assertEquals(5.0, result, 0.001);
    }

    @Test
    public void testEvaluateWithWhitespace() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        String expression = " 3 +    11 / 2  ";
        double result = evaluator.evaluate(expression);
        assertEquals(8.5, result, 0.001);
    }

    @Test
    public void testEvaluateNegativeNumber() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        String expression = "-5+3";
        double result = evaluator.evaluate(expression);
        assertEquals(-2.0, result, 0.001);
    }

    @Test
    public void testEvaluateNegativeMultiplication() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        String expression = "-3*4";
        double result = evaluator.evaluate(expression);
        assertEquals(-12.0, result, 0.001);
    }

    @Test
    public void testEvaluateMultipleNegativeNumbers() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        String expression = "-3*-4";
        double result = evaluator.evaluate(expression);
        assertEquals(12.0, result, 0.001);
    }

    @Test
    public void testEvaluateComplexExpression() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        String expression = "3+5*2/(7-2)";
        double result = evaluator.evaluate(expression);
        assertEquals(5.0, result, 0.001);
    }

    @Test
    public void testEvaluateMultipleOperators() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        String expression = "5-4+3*2";
        double result = evaluator.evaluate(expression);
        assertEquals(7.0, result, 0.001);
    }

    @Test
    public void testEvaluateOnlyNegativeInteger() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        String expression = "-15";
        double result = evaluator.evaluate(expression);
        assertEquals(-15.0, result, 0.001);
    }

    @Test
    public void testEvaluateMultipleSequentialNegatives() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        String expression = "--5";
        double result = evaluator.evaluate(expression);
        assertEquals(5.0, result, 0.001);
    }

    @Test
    public void testEvaluateFloatingPointNumbers() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        String expression = "2.5+3.2";
        double result = evaluator.evaluate(expression);
        assertEquals(5.7, result, 0.001);
    }

    @Test
    public void testEvaluateFloatingPointMultiplication() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        String expression = "1.2*3.4";
        double result = evaluator.evaluate(expression);
        assertEquals(4.08, result, 0.001);
    }

    @Test
    public void testEvaluateMixedIntegerAndFloatingPoint() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        String expression = "5+2.5*2";
        double result = evaluator.evaluate(expression);
        assertEquals(10.0, result, 0.001);
    }

    @Test
    public void testEvaluateComplexFloatingPointExpression() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        String expression = "(1.2+2.8)*3.5";
        double result = evaluator.evaluate(expression);
        assertEquals(14.0, result, 0.001);
    }

    @Test
    public void testEvaluateOperatorPrecedence() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        String expression = "2+3*4/2-1";
        double result = evaluator.evaluate(expression);
        assertEquals(7.0, result, 0.001);
    }
}