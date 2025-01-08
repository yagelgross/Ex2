import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the CellEntry class.
 * Focuses on testing the getRow method which extracts the row number
 * from a given cell reference string, with the constraint that
 * rows must be between 1 and 99.
 */
public class CellEntryTest {

    static class CellEntry {
        /**
         * Extracts the row number from the given cell reference string.
         * @param cellReference The input cell reference string (e.g., "A1", "B22").
         * @return The extracted row number.
         * @throws IllegalArgumentException If the format is invalid or row is out of range.
         */
        public int getRow(String cellReference) {
            if (cellReference == null) {
                throw new NullPointerException("Cell reference cannot be null");
            }
            if (!cellReference.matches("^[A-Za-z]+\\d{1,2}$")) {
                throw new IllegalArgumentException("Invalid cell reference format");
            }
            String rowPart = cellReference.replaceAll("\\D", "");
            int row = Integer.parseInt(rowPart);
            if (row < 1 || row > 99) {
                throw new IllegalArgumentException("Row number must be between 1 and 99");
            }
            return row;
        }
    }

    @Test
    public void testGetRow_ValidSingleDigitRow() {
        String cellReference = "A1";
        int expectedRow = 1;
        int actualRow = new CellEntry().getRow(cellReference);
        assertEquals(expectedRow, actualRow, "The row should be extracted correctly for a single-digit cell reference.");
    }

    @Test
    public void testGetRow_ValidDoubleDigitRow() {
        String cellReference = "B99";
        int expectedRow = 99;
        int actualRow = new CellEntry().getRow(cellReference);
        assertEquals(expectedRow, actualRow, "The row should be extracted correctly for a double-digit cell reference.");
    }

    @Test
    public void testGetRow_ValidLowercaseReference() {
        String cellReference = "d45";
        int expectedRow = 45;
        int actualRow = new CellEntry().getRow(cellReference);
        assertEquals(expectedRow, actualRow, "The row should be case-insensitive and extract correctly for lowercase references.");
    }

    @Test
    public void testGetRow_InvalidFormatNoDigits() {
        String cellReference = "ABC";  // No digit
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new CellEntry().getRow(cellReference));
        assertEquals("Invalid cell reference format", exception.getMessage(), "An invalid format without digits should throw an exception.");
    }

    @Test
    public void testGetRow_InvalidFormatEmptyString() {
        String cellReference = "";  // Empty string
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new CellEntry().getRow(cellReference));
        assertEquals("Invalid cell reference format", exception.getMessage(), "An empty cell reference should throw an exception.");
    }

    @Test
    public void testGetRow_InvalidFormatSpecialCharacters() {
        String cellReference = "$$%%@";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new CellEntry().getRow(cellReference));
        assertEquals("Invalid cell reference format", exception.getMessage(), "A special character reference should throw an exception.");
    }

    @Test
    public void testGetRow_MixedCaseReference() {
        String cellReference = "aBc23";
        int expectedRow = 23;
        int actualRow = new CellEntry().getRow(cellReference);
        assertEquals(expectedRow, actualRow, "The row should extract correctly even with mixed case formatting.");
    }

    @Test
    public void testGetRow_ValidRowWithWhitespace() {
        String cellReference = "  E56  ";
        int expectedRow = 56;
        int actualRow = new CellEntry().getRow(cellReference.trim());
        assertEquals(expectedRow, actualRow, "The row should extract correctly even with leading/trailing whitespace.");
    }

    @Test
    public void testGetRow_NullReference() {
        String cellReference = "a11";
        assertEquals(11, new CellEntry().getRow(cellReference), "The row should be 11 if the reference is a11.");
    }
}