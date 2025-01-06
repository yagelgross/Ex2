import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellEntryTest {

    @Test
    void isValid() {
        // Case: Valid uppercase format
        CellEntry cellEntry1 = new CellEntry() {
            @Override
            public String toString() {
                return "A1";
            }
        };
        assertTrue(cellEntry1.isValid());

        // Case: Valid 2 Digits format
        CellEntry cellEntry1b = new CellEntry() {
            @Override
            public String toString() {
                return "A22";
            }
        };
        assertTrue(cellEntry1b.isValid());

        // Case: Valid lowercase format
        CellEntry cellEntry2 = new CellEntry() {
            @Override
            public String toString() {
                return "z2";
            }
        };
        assertTrue(cellEntry2.isValid());

        // Case: Invalid format (no digits)
        CellEntry cellEntry3 = new CellEntry() {
            @Override
            public String toString() {
                return "A";
            }
        };
        assertFalse(cellEntry3.isValid());

        // Case: Invalid format (digits only)
        CellEntry cellEntry4 = new CellEntry() {
            @Override
            public String toString() {
                return "12";
            }
        };
        assertFalse(cellEntry4.isValid());

        // Case: Invalid format (special characters)
        CellEntry cellEntry5 = new CellEntry() {
            @Override
            public String toString() {
                return "@1";
            }
        };
        assertFalse(cellEntry5.isValid());
    }

    @Test
    void getX() {
        // Case: Uppercase column
        CellEntry cellEntry1 = new CellEntry() {
            @Override
            public String toString() {
                return "A1";
            }
        };
        assertEquals(0, cellEntry1.getX());

        // Case: Lowercase column
        CellEntry cellEntry2 = new CellEntry() {
            @Override
            public String toString() {
                return "b2";
            }
        };
        assertEquals(1, cellEntry2.getX());

        // Case: Multi-digit row
        CellEntry cellEntry3 = new CellEntry() {
            @Override
            public String toString() {
                return "Z34";
            }
        };
        assertEquals(25, cellEntry3.getX());

        // Case: Invalid format (digits only)
        CellEntry cellEntry4 = new CellEntry() {
            @Override
            public String toString() {
                return "12";
            }
        };
        assertEquals(-1 ,cellEntry4.getX());
    }

    @Test
    void getY() {
    }
}