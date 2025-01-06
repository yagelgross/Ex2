import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Ex2SheetTest {

    @Test
    void testEval_NumericValue() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "42");
        String result = sheet.eval(0, 0);
        assertEquals("42", result, "Cell with numeric value should return the value as a string");
    }

    @Test
    void testEval_StringValue() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "Hello");
        String result = sheet.eval(0, 0);
        assertEquals("Hello", result, "Cell with string value should return the value as a string");
    }

    @Test
    void testEval_SingleEqualsFormula() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "=5+5");
        String result = sheet.eval(0, 0);
        assertEquals("10.0", result, "Cell with '=5+5' formula should return '10.0'");
    }

    @Test
    void testEval_ComplexFormula() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "=12/4+6*2");
        String result = sheet.eval(0, 0);
        assertEquals("15.0", result, "Cell with '=12/4 + 6*2' formula should return '15.0'");
    }

    @Test
    void testEval_InvalidFormula() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "=InvalidFormula");
        String result = sheet.eval(0, 0);
        assertEquals("Error", result, "Cell with invalid formula should return 'Error'");
    }

    @Test
    void testEval_MalformedFormula() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "=5+");
        String result = sheet.eval(0, 0);
        assertEquals("Error", result, "Cell with malformed formula should return 'Error'");
    }

    @Test
    void testEval_ReferenceToEmptyCell() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "=A2");
        String result = sheet.eval(0, 0);
        assertEquals("Error", result, "Referencing an empty cell should return 'Error'");
    }

    @Test
    void testEval_ReferenceToNumericCell() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(1, 0, "25");
        sheet.set(0, 0, "=A2");
        String result = sheet.eval(0, 0);
        assertEquals("25", result, "Referencing a numeric cell should return the numeric value as a string");
    }

    @Test
    void testEval_CircularReference() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "=A2");
        sheet.set(1, 0, "=A1");
        String result = sheet.eval(0, 0);
        assertEquals("Error", result, "Circular reference should return 'Error'");
    }
    @Test
    void testEvaluate_WithValidDepths() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "=5+5");
        sheet.set(1, 1, "=2*3");
        sheet.set(2, 2, "=10/2");

        int[][] depths = {
                {0, -1, -1},
                {-1, 1, -1},
                {-1, -1, 2}
        };

        sheet.evaluate(depths);
        assertEquals("10.0", sheet.value(0, 0), "Depth 0 cell should evaluate correctly");
        assertEquals("6.0", sheet.value(1, 1), "Depth 1 cell should evaluate correctly");
        assertEquals("5.0", sheet.value(2, 2), "Depth 2 cell should evaluate correctly");
    }

    @Test
    void testEvaluate_WithInvalidDepths() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "=5+5");

        int[][] invalidDepths = {
                {0, -1}, // Invalid row size
                {-1, 1, -1}
        };

        assertThrows(IllegalArgumentException.class, () -> sheet.evaluate(invalidDepths), "Invalid depth matrix should throw IllegalArgumentException");
    }
    @Test
    void testEvaluate_WithEmptyDepths() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "=5+5");
    }
    @Test
    void testValue_EmptyCell() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        String result = sheet.value(0, 0);
        assertEquals(Ex2Utils.EMPTY_CELL, result, "Empty cell should return the EMPTY_CELL constant value.");
    }

    @Test
    void testValue_OutOfBoundsCell() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        String result = sheet.value(3, 3);
        assertEquals(Ex2Utils.EMPTY_CELL, result, "Out-of-bounds cell should return the EMPTY_CELL constant value.");
    }

    @Test
    void testValue_NumericCell() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(1, 1, "123");
        String result = sheet.value(1, 1);
        assertEquals("123", result, "Numeric cell should return its set value as a string.");
    }

    @Test
    void testValue_StringCell() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "Hello");
        String result = sheet.value(0, 0);
        assertEquals("Hello", result, "String cell should return its set value as a string.");
    }

    @Test
    void testValue_FormulaEvaluation() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "=2*3");
        String result = sheet.value(0, 0);
        assertEquals("6.0", result, "Formula cell should return the evaluated result as a string.");
    }
}