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
}