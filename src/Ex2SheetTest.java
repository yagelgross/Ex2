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
        sheet.set(0, 0, "=12/4 + 6*2");
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
}