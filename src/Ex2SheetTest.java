import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class Ex2SheetTest {

    /**
     * Tests for the evaluate method in the Ex2Sheet class.
     * <p>
     * The evaluate method calculates the value of a cell in the spreadsheet.
     * It evaluates cell content, resolves formulas, and handles errors such as
     * cyclic dependencies or malformed expressions.
     */

    @Test
    public void testEvaluate_NumberCell() {
        // Test case: A cell containing a simple number
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "123");
        assertEquals("123.0", sheet.eval(0, 0));
    }

    @Test
    public void testEvaluate_EmptyCell() {
        // Test case: An empty cell should evaluate to an empty string
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        assertEquals("", sheet.eval(1, 1));
    }

    @Test
    public void testEvaluate_TextCell() {
        // Test case: A cell containing plain text
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(2, 2, "Hello");
        assertEquals("Hello", sheet.eval(2, 2));
    }

    @Test
    public void testEvaluate_SimpleFormula_Addition() {
        // Test case: A cell containing a formula for addition
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "5");
        sheet.set(1, 0, "10");
        sheet.set(2, 0, "=A0 + B0"); // Formula referring to other cells
        assertEquals("15.0", sheet.eval(2, 0));
    }

    @Test
    public void testEvaluate_MalformedFormula() {
        // Test case: A cell containing a malformed formula
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "=5 +");
        assertEquals(Ex2Utils.ERR_FORM, sheet.eval(0, 0));
    }

    @Test
    public void testEvaluate_CyclicDependency() {
        // Test case: A cycle in formulas should return ERR_CYCLE
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "=B0");
        sheet.set(1, 0, "=A0"); // Cycle between A1 and B1
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.eval(0, 0));
    }

    @Test
    public void testEvaluate_ReferenceTextCell() {
        // Test case: Formula referencing a cell with non-numeric/text data
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "Hello");
        sheet.set(1, 0, "=A1");
        assertEquals(Ex2Utils.ERR_FORM, sheet.eval(1, 0));
    }

    @Test
    public void testEvaluate_ComplexFormulaWithParentheses() {
        // Test case: A cell containing a complex formula with parentheses
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "=2 * (3 + 4)");
        assertEquals("14.0", sheet.eval(0, 0));
    }

    @Test
    public void testEvaluate_FormulaWithInvalidReference() {
        // Test case: Formula with an invalid reference should return ERR_FORM
        Ex2Sheet sheet = new Ex2Sheet(26, 100);
        sheet.set(0, 0, "=Z99 + 5"); // Z99 is out of bounds
        assertEquals(Ex2Utils.ERR_FORM, sheet.eval(0, 0));
    }

    @Test
    public void testEvaluate_DependentCellUpdates() {
        // Test case: Updates in one cell affecting a dependent cell
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "5");
        sheet.set(1, 0, "=A0 + 10");
        assertEquals("15.0", sheet.eval(1, 0));
        sheet.set(0, 0, "20"); // Update A1
        assertEquals("30.0", sheet.eval(1, 0)); // Dependent cell should reflect the update
    }
    @Test
    public void testSave_SimpleSheet() throws Exception {
        // Test case: Save a simple spreadsheet to a file
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "123");
        sheet.set(1, 1, "Hello");
        sheet.set(2, 2, "=A1 + 2");

        String tempFile = Files.createTempFile("sheet", ".txt").toString();
        sheet.save(tempFile);

        String content = Files.readString(Paths.get(tempFile));
        assertTrue(content.contains("0,0,123"));
        assertTrue(content.contains("1,1,Hello"));
        assertTrue(content.contains("2,2,=A1 + 2"));

        Files.delete(Paths.get(tempFile));
    }

    @Test
    public void testSaveAndLoad_RetainsData() throws Exception {
        // Test case: Save and reload a spreadsheet, ensuring data is retained
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "456");
        sheet.set(1, 1, "World");
        sheet.set(2, 2, "=B2 * 3");

        String tempFile = Files.createTempFile("sheet", ".txt").toString();
        sheet.save(tempFile);

        Ex2Sheet loadedSheet = new Ex2Sheet(5, 5);
        loadedSheet.load(tempFile);

        assertEquals("456.0", loadedSheet.eval(0, 0));
        assertEquals("World", loadedSheet.eval(1, 1));
        assertEquals(Ex2Utils.ERR_FORM, loadedSheet.eval(2, 2)); // Dependent eval should be handled after load

        Files.delete(Paths.get(tempFile));
    }
    @Test
    public void testDepth_NoDependencies() {
        // Test case: No dependencies in cells
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "10");
        sheet.set(1, 1, "20");
        int[][] depths = sheet.depth();
        assertEquals(0, depths[0][0]); // No dependencies
        assertEquals(0, depths[1][1]); // No dependencies
    }

    @Test
    public void testDepth_SingleLevelDependency() {
        // Test case: Cells with direct dependencies
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "10");
        sheet.set(1, 1, "=A0");
        int[][] depths = sheet.depth();
        assertEquals(0, depths[0][0]); // A0 is a value
        assertEquals(1, depths[1][1]); // B2 depends on A0
    }

    @Test
    public void testSave_SheetWithMixedContent() throws Exception {
        // Test case: Save a sheet with mixed cell content
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "42");
        sheet.set(1, 1, "=A1 + 5");
        sheet.set(2, 2, "Hello");

        String tempFile = Files.createTempFile("mixed_sheet", ".txt").toString();
        sheet.save(tempFile);

        String content = Files.readString(Paths.get(tempFile));
        assertTrue(content.contains("0,0,42"));
        assertTrue(content.contains("1,1,=A1 + 5"));
        assertTrue(content.contains("2,2,Hello"));

        Files.delete(Paths.get(tempFile));
    }

    @Test
    public void testSave_LongSheet() throws Exception {
        // Test case: Save a larger sheet with multiple cells filled
        Ex2Sheet sheet = new Ex2Sheet(26, 100);
        sheet.set(0, 0, "100");
        sheet.set(24, 49, "=A0 * 2");
        sheet.set(25, 25, "Text");

        String tempFile = Files.createTempFile("long_sheet", ".txt").toString();
        sheet.save(tempFile);

        Path path = Paths.get(tempFile);
        String content = Files.readString(path);
        assertTrue(content.contains("0,0,100"));
        assertTrue(content.contains("24,49,=A0 * 2"));
        assertTrue(content.contains("25,25,Text"));

        Files.delete(path);
    }
}