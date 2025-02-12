import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SCellTest {

    /**
     * Test class for the `isFormula` method in the `SCell` class.
     * The `isFormula` method checks if a given string is a valid formula.
     * A valid formula starts with '=' and follows specific syntax rules for numbers, cell references, and formulas.
     */

    @Test
    public void testIsFormula() {
        String input1 = "=5+3";
        assertTrue(SCell.isFormula(input1), "The input should be identified as a valid formula.");

        String input2 = "=(AB5+3)*2";
        assertFalse(SCell.isFormula(input2), "The input should be identified as an invalid formula.");

        String input3 = "=A1+B+B2";
        assertFalse(SCell.isFormula(input3), "The input should be identified as a valid formula with cell references.");

        String input4 = "5+3";
        assertFalse(SCell.isFormula(input4), "The input should not be identified as a formula because it does not start with '='.");

        String input5 = "=5+3&";
        assertFalse(SCell.isFormula(input5), "The input should not be identified as a formula due to invalid characters.");

        String input6 = "=";
        assertFalse(SCell.isFormula(input6), "The input should not be identified as a valid formula because it has no content after '='.");

        String input7 = "";
        assertFalse(SCell.isFormula(input7), "The input should not be identified as a formula because it is empty.");

        String input8 = "=((A1+3)*2)";
        assertTrue(SCell.isFormula(input8), "The input should be identified as a valid formula with parentheses and operators.");

        String input9 = "=1.5+2.3";
        assertTrue(SCell.isFormula(input9), "The input should be identified as a valid formula with decimal numbers.");

        String input10 = "=(5+3";
        assertFalse(SCell.isFormula(input10), "The input should not be identified as a valid formula due to mismatched parentheses.");
    }

    @Test
    public void testComputeForm() {
        String validFormula1 = "=5+3";
        assertEquals("8.0", SCell.computeForm(validFormula1), "The formula should compute to 8.");

        String validFormula2 = "=10/2";
        assertEquals("5.0", SCell.computeForm(validFormula2), "The formula should compute to 5.");

        String validFormula3 = "=2*3+4";
        assertEquals("10.0", SCell.computeForm(validFormula3), "The formula should compute to 10.");

        String validFormula4 = "=(2+3)*4";
        assertEquals("20.0", SCell.computeForm(validFormula4), "The formula should compute to 20.");

        String invalidFormula2 = "=5+*3";
        assertEquals(Ex2Utils.ERR_FORM, SCell.computeForm(invalidFormula2), "Invalid syntax should return an error.");

        String invalidFormula3 = "5+3";
        assertEquals(Ex2Utils.ERR_FORM, SCell.computeForm(invalidFormula3), "Missing '=' should return an error.");

        String invalidFormula4 = "=(5+3))";
        assertEquals(Ex2Utils.ERR_FORM, SCell.computeForm(invalidFormula4), "Mismatched parentheses should return an error.");
    }

    @Test
    public void testValidFormulas() {
        assertEquals("8.0", SCell.computeForm("=5+3"), "The formula '=5+3' should evaluate to 8.0.");
        assertEquals("10.0", SCell.computeForm("=(2+8)"), "The formula '=(2+8)' should evaluate to 10.0.");
        assertEquals("6.0", SCell.computeForm("=(2*3)"), "The formula '=(2*3)' should evaluate to 6.0.");
        assertEquals("7.25", SCell.computeForm("=1+2*3.125"), "The formula '=1+2*3.125' should evaluate to 7.25.");
        assertEquals("16.0", SCell.computeForm("=(4*(2+2))"), "The formula '=(4*(2+2))' should evaluate to 16.0.");
    }

    @Test
    public void testInvalidFormulas() {
        assertEquals(Ex2Utils.ERR_FORM, SCell.computeForm("5+3"), "The input '5+3' should be identified as invalid.");
        assertEquals(Ex2Utils.ERR_FORM, SCell.computeForm("=A1+B3"), "Invalid references like 'A1+B3' should return error.");
        assertEquals(Ex2Utils.ERR_FORM, SCell.computeForm("=5+*3"), "Invalid syntax like '=5+*3' should result in error.");
        assertEquals(Ex2Utils.ERR_FORM, SCell.computeForm("=(5+3))"), "Mismatched parentheses should result in an error.");
        assertEquals(Ex2Utils.ERR_FORM, SCell.computeForm("=(3+)"), "Incomplete formulas should return an error.");
    }
}
