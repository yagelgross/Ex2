import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Ex2SheetTest {

    @Test
    void testConstructorAndDimensions() {
        Ex2Sheet sheet = new Ex2Sheet(5, 3); // 5 columns, 3 rows.
        assertEquals(5, sheet.width(), "Width should be 5");
        assertEquals(3, sheet.height(), "Height should be 3");
    }

    @Test
    void testSetAndGetCell() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(2, 3, "42");
        assertEquals("42", sheet.get(2, 3).getData(), "Cell at (2, 3) should contain '42'");
    }

    @Test
    void testIsIn() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        assertTrue(sheet.isIn(0, 0), "Top-left corner should be valid.");
        assertTrue(sheet.isIn(4, 4), "Bottom-right corner should be valid.");
        assertTrue(sheet.isIn(5, 5), "Out-of-bounds should be invalid.");
        assertFalse(sheet.isIn(-1, 0), "Negative index should be invalid.");
    }

    @Test
    void testSetAndEvalFormula() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "=2+3");
        assertEquals("5.0", sheet.eval(0, 0), "Formula '=2+3' should evaluate to 5.0");
    }

    @Test
    void testSetAndEvalWithDependencies() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "10");
        sheet.set(0, 1, "=A0+5");
        assertEquals("15.0", sheet.eval(0, 1), "Formula '=A1+5' should evaluate to 15.0");
    }

    @Test
    void testCircularDependency() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "=b0");
        sheet.set(1, 0, "=a0");
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.eval(0, 0));
    }


    @Test
    void testEvaluateEntireSheet() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "10");
        sheet.set(1, 0, "=A0+5");
        sheet.set(2, 0, "=B0*2");
        sheet.eval(); // Evaluate the entire sheet
        assertEquals("10.0", sheet.eval(0, 0));
        assertEquals("15.0", sheet.eval(1, 0), "Cell B1 should evaluate to 15.0.");
        assertEquals("30.0", sheet.eval(2, 0), "Cell C1 should evaluate to 30.0.");
    }

    @Test
    void testInvalidFormula() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "=INVALID");
        assertEquals(Ex2Utils.ERR_FORM, sheet.eval(0, 0), "Invalid formula should return '#ERR'.");
    }
    @Test
    void testSetOverwriteCell() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(1, 1, "100");
        assertEquals("100.0", sheet.eval(1, 1), "Cell should initially contain '100'");
        sheet.set(1, 1, "=A0*2");
        assertEquals(Ex2Utils.ERR_FORM, sheet.eval(1, 1), "Overwriting formula should re-evaluate correctly");
    }
    @Test
    void testSetCell2() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(1, 1, "100");
        assertEquals("100.0", sheet.eval(1, 1), "Cell should initially contain '100'");
        sheet.set(1, 1, "=A0*2"); // Reference to A0 which has no value
        assertEquals(Ex2Utils.ERR_FORM, sheet.eval(1, 1), "Overwriting with formula referencing invalid cell should return ERR_FORM");
    }

    @Test
    void testEvaluateCellWithSelfReference() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(1, 1, "=B1"); // Self-reference
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.eval(1, 1), "Self-referencing formulas should return ERR_CYCLE");
    }

    @Test
    void testEvaluateCellWithDeepDependencies() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "10");
        sheet.set(1, 0, "=A0+5");  // Depends on A0
        sheet.set(2, 0, "=B0*2");  // Depends on B0, which depends on A0
        sheet.set(3, 0, "=C0-7");  // Depends on C0, which depends on B0
        assertEquals("10.0", sheet.eval(0, 0));
        assertEquals("15.0", sheet.eval(1, 0));
        assertEquals("30.0", sheet.eval(2, 0));
        assertEquals("23.0", sheet.eval(3, 0));
    }

    @Test
    void testIsInEdgeCase() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        assertTrue(sheet.isIn(4, 4));
        assertTrue(sheet.isIn(5, 5));
        assertFalse(sheet.isIn(-1, 0), "Negative row index should be out of bounds.");
        assertFalse(sheet.isIn(0, -1), "Negative column index should be out of bounds.");
    }

    @Test
    void testMixedNumericAndStringValues() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "42");
        sheet.set(0, 1, "Hello");
        sheet.set(0, 2, "=A0*2");  // Valid formula
        sheet.set(0, 3, "=a1+1");  // Invalid formula: B1 is text
        assertEquals("42.0", sheet.eval(0, 0), "Cell A0 should evaluate to its numeric value.");
        assertEquals("Hello", sheet.eval(0, 1), "Cell B0 should evaluate to its text value.");
        assertEquals("84.0", sheet.eval(0, 2), "Cell A2 should evaluate formula '=A0*2' to 84.");
        assertEquals(Ex2Utils.ERR_FORM, sheet.eval(0, 3), "Cell A3 referencing a text cell should return ERR_FORM.");
    }

    @Test
    void testFormulaWithParentheses() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "=((2+3)*4)");
        assertEquals("20.0", sheet.eval(0, 0));
        sheet.set(0, 1, "=((2+3)*(4+5))");
        assertEquals("45.0", sheet.eval(0, 1));
        sheet.set(0, 2, "=((2+3)*(4+5)/(6+7))");
        assertEquals("3.4615384615384617", sheet.eval(0, 2));
        sheet.set(0, 3, "=((2+3)*(4+5)/((6+7)*(8+9)))");
        assertEquals("0.20361990950226244", sheet.eval(0, 3));
        sheet.set(0, 4, "=((2+3)*(4+5)/(6+7)*(8+9)/(10+11))");
        assertEquals("2.802197802197802", sheet.eval(0, 4));
        sheet.set(1, 3, "=((2+3)*(4+5)/(6+7)*(8+9)/(10+11)*(12+13))");
        assertEquals("70.05494505494505", sheet.eval(1, 3));
        sheet.set(1, 2, "=((2+3)*(4+5)/(6+7)*(8+9)/(10+11)*(12+13)*(14+15))");
        assertEquals("2031.5934065934066", sheet.eval(1, 2));
    }

    @Test
    void testComplexFormulaWithDependencies() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "5");
        sheet.set(1, 0, "2");
        sheet.set(0, 1, "=(A0+B0)*(A0-B0)");
        assertEquals("21.0", sheet.eval(0, 1));
    }

    @Test
    void testEmptyCellHandling() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        assertEquals("", sheet.eval(3, 3));
        sheet.set(3, 3, "");
        assertEquals("", sheet.eval(3, 3));
    }

    @Test
    void testWhitespaceCellHandling() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(3, 3, "    ");
        assertEquals("    ", sheet.eval(3, 3));
    }

    @Test
    void testDependencyLoopAcrossMultipleCells() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "=B0");
        sheet.set(1, 0, "=C0");
        sheet.set(2, 0, "=A0");
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.eval(0, 0), "Dependency loop (A0 -> B0 -> C0 -> A0) should return ERR_CYCLE for A0.");
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.eval(1, 0), "Dependency loop should return ERR_CYCLE for B0.");
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.eval(2, 0), "Dependency loop should return ERR_CYCLE for C0.");
    }

    @Test
    void testValueForEmptyCell() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        assertEquals("",sheet.value(2, 2));
    }

    @Test
    void testValueForStaticValueInCell() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(1, 1, "42");
        assertEquals("42.0", sheet.value(1, 1));
    }

    @Test
    void testValueForFormulaInCell() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "=2+3");
        assertEquals("5.0", sheet.value(0, 0));
    }

    @Test
    void testValueForDependencyFormula() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "10");
        sheet.set(1, 0, "=A0+5");
        assertEquals("15.0", sheet.value(1, 0));
    }

    @Test
    void testValueForCircularDependency() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "=B0");
        sheet.set(1, 0, "=A0");
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.value(0, 0));
    }

    @Test
    void testValueForInvalidFormula() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "=INVALID");
        assertEquals(Ex2Utils.ERR_FORM, sheet.value(0, 0));
    }

}