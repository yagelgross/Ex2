import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellEntryTest {

    @Test
    void isValid() {
        // Case: Valid uppercase format
        SCell cellEntry1 = new SCell("=A1") {
            @Override
            public String toString() {
                return getData();
            }
        };
        assertTrue(cellEntry1.isValid());

        // Case: Valid 2 Digits format
        SCell cellEntry2 = new SCell("A22") {
            @Override
            public String toString() {
                return getData();
            }
        };
        assertTrue(cellEntry2.isValid());

        // Case: Valid lowercase format
        SCell cellEntry3 = new SCell("z2") {
            @Override
            public String toString() {
                return getData();
            }
        };
        assertTrue(cellEntry3.isValid());

        // Case: Invalid format (no digits)
        SCell cellEntry4 = new SCell("A") {
            @Override
            public String toString() {
                return getData();
            }
        };
        assertFalse(cellEntry4.isValid());

        // Case: invalid lower letter format
        SCell cellEntry5 = new SCell("a") {
            @Override
            public String toString() {
                return getData();
            }
        };
        assertFalse(cellEntry5.isValid());



        // Case: Invalid format (digits only)
        CellEntry cellEntry6 = new CellEntry() {
            @Override
            public String toString() {
                return "12";
            }
        };
        assertFalse(cellEntry6.isValid());

        // Case: Invalid format (special characters)
        CellEntry cellEntry7 = new CellEntry() {
            @Override
            public String toString() {
                return "@1";
            }
        };
        assertFalse(cellEntry7.isValid());

        // Case: Multiple uppercase letters followed by digits
        CellEntry cellEntry15 = new CellEntry() {
            @Override
            public String toString() {
                return "ABC12";
            }
        };
        assertFalse(cellEntry15.isValid());

        // Case: Multiple lowercase letters followed by digits
        CellEntry cellEntry16 = new CellEntry() {
            @Override
            public String toString() {
                return "abc34";
            }
        };
        assertFalse(cellEntry16.isValid());

        // Case: Mixed-case letters followed by digits
        CellEntry cellEntry17 = new CellEntry() {
            @Override
            public String toString() {
                return "AbC56";
            }
        };
        assertFalse(cellEntry17.isValid());

        // Case: Alphanumeric starting with numbers
        CellEntry cellEntry18 = new CellEntry() {
            @Override
            public String toString() {
                return "1A2";
            }
        };
        assertFalse(cellEntry18.isValid());

        // Case: Alphanumeric starting with numbers and ending in letters
        CellEntry cellEntry19 = new CellEntry() {
            @Override
            public String toString() {
                return "2b3";
            }
        };
        assertFalse(cellEntry19.isValid());

        // Case: Valid single-letter uppercase format
        CellEntry cellEntry8 = new CellEntry() {
            @Override
            public String toString() {
                return "C3";
            }
        };
        assertTrue(cellEntry8.isValid());

        // Case: Valid single-letter lowercase format
        CellEntry cellEntry9 = new CellEntry() {
            @Override
            public String toString() {
                return "d4";
            }
        };
        assertTrue(cellEntry9.isValid());

        // Case: Valid mixed uppercase and lowercase column letters
        CellEntry cellEntry10 = new CellEntry() {
            @Override
            public String toString() {
                return "e12";
            }
        };
        assertTrue(cellEntry10.isValid());

        // Case: Invalid format with whitespace
        CellEntry cellEntry11 = new CellEntry() {
            @Override
            public String toString() {
                return " A1";
            }
        };
        assertFalse(cellEntry11.isValid());

        // Case: Invalid format with multiple alphanumeric characters
        CellEntry cellEntry12 = new CellEntry() {
            @Override
            public String toString() {
                return "XX1";
            }
        };
        assertFalse(cellEntry12.isValid());

        // Case: Empty string
        CellEntry cellEntry13 = new CellEntry() {
            @Override
            public String toString() {
                return "";
            }
        };
        assertFalse(cellEntry13.isValid());

        // Case: Null string
        CellEntry cellEntry14 = new CellEntry() {
            @Override
            public String toString() {
                return null;
            }
        };
        assertFalse(cellEntry14.isValid());
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
        // Case: Valid uppercase format
        CellEntry cellEntry1 = new CellEntry() {
            @Override
            public String toString() {
                return "A1";
            }
        };
        assertEquals(1, cellEntry1.getY());

        // Case: Valid lowercase format
        CellEntry cellEntry2 = new CellEntry() {
            @Override
            public String toString() {
                return "b22";
            }
        };
        assertEquals(22, cellEntry2.getY());

        // Case: Multi-digit row with uppercase
        CellEntry cellEntry3 = new CellEntry() {
            @Override
            public String toString() {
                return "Z999";
            }
        };
        assertEquals(-1, cellEntry3.getY());

        // Case: Invalid format (digits only)
        CellEntry cellEntry4 = new CellEntry() {
            @Override
            public String toString() {
                return "12";
            }
        };
        assertEquals(-1, cellEntry4.getY());

        // Case: Invalid format (alphabet only)
        CellEntry cellEntry5 = new CellEntry() {
            @Override
            public String toString() {
                return "A";
            }
        };
        assertEquals(-1, cellEntry5.getY());

        // Case: Invalid format (special characters)
        CellEntry cellEntry6 = new CellEntry() {
            @Override
            public String toString() {
                return "#7";
            }
        };
        assertEquals(-1, cellEntry6.getY());
    }
}