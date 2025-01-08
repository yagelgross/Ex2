import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class Ex2SheetTest {

    @Test
    void testEval() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);

        // Existing tests for numeric, string, and formulas
        sheet.set(0, 0, "42");
        assertEquals("42", sheet.eval(0, 0), "Cell with numeric value should return the value as a string");

        sheet.set(0, 0, "=5+5");
        assertEquals("10.0", sheet.eval(0, 0), "'=5+5' formula should return '10.0'");

        // Extreme numeric formulas
        sheet.set(0, 0, "=1000000000*1000000000");
        assertEquals("1.0E18", sheet.eval(0, 0), "Should handle very large numbers properly");

        sheet.set(0, 0, "=-5+-10");
        assertEquals("ERR_FORM!", sheet.eval(0, 0));

        sheet.set(0, 0, "=1/0");
        assertEquals("ERR_FORM!", sheet.eval(0, 0), "Division by zero should return 'ERR_FORM!'");

        // Invalid cell references
        sheet.set(0, 0, "=InvalidCell");
        assertEquals("ERR_FORM!", sheet.eval(0, 0), "Referencing an invalid cell name should return 'Error'");

        sheet.set(0, 0, "=Z100");
        assertEquals("ERR_FORM!", sheet.eval(0, 0), "Referencing an out-of-bounds cell should return 'Error'");

        sheet.set(0, 0, "=10");
        sheet.set(1, 0, "=A1");
        assertEquals("10.0", sheet.eval(1, 0));
        // Recursive circular reference spanning multiple layers
        sheet.set(0, 0, "=A2");
        sheet.set(1, 0, "=A3");
        sheet.set(2, 0, "=A1");
        assertEquals("ERR_CYCLE!", sheet.eval(0, 0), "Recursive circular references should return 'ERR_CYCLE!'");
    }

    @Test
    void testEvaluate() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);

        // Valid depths
        sheet.set(0, 0, "=5+5");
        sheet.set(1, 1, "=2*3");
        sheet.set(2, 2, "=10/2");
        int[][] depths = {
                {0, -1, -1},
                {-1, 1, -1},
                {-1, -1, 2}
        };
        sheet.evaluate(depths);
        assertEquals("10.0", sheet.value(0, 0));
        assertEquals("12.0", sheet.value(1, 1), "Ensure formula calculation matches SCell calculation.");
        assertEquals("5.0", sheet.value(2, 2));

        // Test invalid depths
        int[][] invalidDepths = {
                {0, -1},
                {-1, 1, -1}
        };
        assertThrows(IllegalArgumentException.class, () -> sheet.evaluate(invalidDepths));
    }

    @Test
    void testValue() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);

        // Test empty and out-of-bounds cells
        assertEquals(Ex2Utils.EMPTY_CELL, sheet.value(0, 0), "Empty cell should return the EMPTY_CELL constant value.");
        assertEquals(Ex2Utils.EMPTY_CELL, sheet.value(3, 3), "Ensure out-of-bounds access returns EMPTY_CELL.");

        // Test numeric and string cells
        sheet.set(1, 1, "123");
        assertEquals("123", sheet.value(1, 1));

        sheet.set(0, 0, "Hello");
        assertEquals("Hello", sheet.value(0, 0));

        // Test formula evaluation
        sheet.set(0, 0, "=2*3");
        assertEquals("6.0", sheet.value(0, 0), "Formula should calculate correctly using SCell's logic.");
    }

    @Test
    void testDepth() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);

        // Test depth for simple numeric cells
        sheet.set(0, 0, "123");
        sheet.set(0, 1, "456");
        int[][] depthResult = sheet.depth();
        assertEquals(0, depthResult[0][0]);
        assertEquals(0, depthResult[0][1]);
        assertEquals(0, depthResult[1][1]);

        // Test depth for simple formulae
        sheet.set(1, 1, "=A1 + A2");
        depthResult = sheet.depth();
        assertEquals(0, depthResult[0][0]);
        assertEquals(0, depthResult[1][1]);

        // Test dependency changes
        sheet.set(1, 0, "=B2");
        depthResult = sheet.depth();
        assertEquals(0, depthResult[0][0]);
        assertEquals(-1, depthResult[1][0]); // Circular dependency invalid

        // Test invalid references
        sheet.set(0, 2, "=Z99");
        depthResult = sheet.depth();
        assertEquals(-2, depthResult[0][2]);
    }

    @Test
    void testLoad() throws IOException {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);

        // Test valid file
        File tempFile = File.createTempFile("valid-sheet", ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("0,0,Hello\n");
            writer.write("1,1,123\n");
            writer.write("2,2,=5+5\n");
        }
        sheet.load(tempFile.getAbsolutePath());
        assertEquals("Hello", sheet.value(0, 0));
        assertEquals("123", sheet.value(1, 1));
        assertEquals("10.0", sheet.value(2, 2), "Ensure the formula evaluates correctly as per SCell.");
        assertTrue(tempFile.delete(), "Ensure temporary file is deleted after testing.");

        // Test file not found
        assertThrows(IOException.class, () -> sheet.load("non_existent_file.txt"));
    }
}