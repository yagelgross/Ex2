import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CellEntryTest {

    /**
     * Test class for the CellEntry class.
     * Specifically, this class focuses on testing the getRow method which extracts the
     * row number from a given cell reference string.
     * Includes tests for valid, invalid, and edge-case scenarios.
     */

    @Test
    public void testGetRow_ValidSingleDigitRow() {
        // Test case where the cell reference has a single-digit row
        String cellRef = "A1";
        int expectedRow = 0;
        int actualRow = CellEntry.getRow(cellRef);
        assertEquals(expectedRow, actualRow, "getRow should return 0 for cell reference A1");
    }

    @Test
    public void testGetRow_ValidDoubleDigitRow() {
        // Test case where the cell reference has a double-digit row
        String cellRef = "B12";
        int expectedRow = 11;
        int actualRow = CellEntry.getRow(cellRef);
        assertEquals(expectedRow, actualRow, "getRow should return 11 for cell reference B12");
    }

    @Test
    public void testGetRow_ValidTripleDigitRow() {
        // Test case where the cell reference has a triple-digit row
        String cellRef = "C105";
        int expectedRow = 104;
        int actualRow = CellEntry.getRow(cellRef);
        assertEquals(expectedRow, actualRow, "getRow should return 104 for cell reference C105");
    }

    @Test
    public void testGetRow_ValidLowercaseReference() {
        // Test case where the cell reference is in lowercase
        String cellRef = "d7";
        int expectedRow = 6;
        int actualRow = CellEntry.getRow(cellRef);
        assertEquals(expectedRow, actualRow, "getRow should return 6 for cell reference d7");
    }

    @Test
    public void testGetRow_InvalidFormatNoDigits() {
        // Test case with an invalid format where there are no digits in the cell reference
        String cellRef = "X";
        assertThrows(NumberFormatException.class, () -> CellEntry.getRow(cellRef),
                "getRow should throw NumberFormatException for cell reference without digits");
    }

    @Test
    public void testGetRow_InvalidFormatEmptyString() {
        // Test case with an empty string as the cell reference
        String cellRef = "";
        assertThrows(NumberFormatException.class, () -> CellEntry.getRow(cellRef),
                "getRow should throw NumberFormatException for empty cell reference");
    }

    @Test
    public void testGetRow_InvalidFormatSpecialCharacters() {
        // Test case with special characters in the cell reference
        String cellRef = "$#@!";
        assertThrows(NumberFormatException.class, () -> CellEntry.getRow(cellRef),
                "getRow should throw NumberFormatException for cell reference with no numbers");
    }

    @Test
    public void testGetRow_MixedCaseReference() {
        // Test case where the cell reference is in mixed case
        String cellRef = "eF45";
        int expectedRow = 44;
        int actualRow = CellEntry.getRow(cellRef);
        assertEquals(expectedRow, actualRow, "getRow should return 44 for cell reference eF45");
    }

    @Test
    public void testGetRow_ValidRowWithWhitespace() {
        // Test case where the cell reference contains leading/trailing whitespace
        String cellRef = " G20 ";
        int expectedRow = 19;
        int actualRow = CellEntry.getRow(cellRef.trim());
        assertEquals(expectedRow, actualRow, "getRow should return 19 for cell reference with whitespace G20");
    }

    @Test
    public void testGetRow_NullReference() {
        // Test case where the cell reference is null
        String cellRef = null;
        assertThrows(NullPointerException.class, () -> CellEntry.getRow(cellRef),
                "getRow should throw NullPointerException for null cell reference");
    }
}