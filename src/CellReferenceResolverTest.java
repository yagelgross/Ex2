import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellReferenceResolverTest {

    @Test
    void testResolveCellReference_ValidReferenceA1() {
        // Arrange
        String reference = "A1";

        // Act
        int[] result = CellReferenceResolver.resolveCellReference(reference);

        // Assert
        assertArrayEquals(new int[]{0, 1}, result);
    }

    @Test
    void testResolveCellReference_ValidReferenceZ99() {
        // Arrange
        String reference = "Z99";

        // Act
        int[] result = CellReferenceResolver.resolveCellReference(reference);

        // Assert
        assertArrayEquals(new int[]{25, 99}, result);
    }

    @Test
    void testResolveCellReference_InvalidReference_NullInput() {
        // Arrange
        String reference = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> CellReferenceResolver.resolveCellReference(reference));
    }

    @Test
    void testResolveCellReference_InvalidReference_EmptyString() {
        // Arrange
        String reference = "";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> CellReferenceResolver.resolveCellReference(reference));
    }

    @Test
    void testResolveCellReference_InvalidReference_FormatError() {
        // Arrange
        String reference = "1A";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> CellReferenceResolver.resolveCellReference(reference));
    }

    @Test
    void testResolveCellReference_InvalidReference_OutOfBoundsRow() {
        // Arrange
        String reference = "A100";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> CellReferenceResolver.resolveCellReference(reference));
    }

    @Test
    void testResolveCellReference_InvalidReference_OutOfBoundsColumn() {
        // Arrange
        String reference = "AA1";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> CellReferenceResolver.resolveCellReference(reference));
    }

    @Test
    void testResolveCellReference_ValidReference_MinimalEdge() {
        // Arrange
        String reference = "A0";

        // Act
        int[] result = CellReferenceResolver.resolveCellReference(reference);

        // Assert
        assertArrayEquals(new int[]{0, 0}, result);
    }

    @Test
    void testResolveCellReference_ValidReference_MaximalEdge() {
        // Arrange
        String reference = "Z99";

        // Act
        int[] result = CellReferenceResolver.resolveCellReference(reference);

        // Assert
        assertArrayEquals(new int[]{25, 99}, result);
    }

    @Test
    void testResolveCellReference_HandlesMultiLetterColumn() {
        // Arrange
        String reference = "AA1";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> CellReferenceResolver.resolveCellReference(reference));
    }

    @Test
    void testResolveCellReference_HandlesWhitespaceInput() {
        // Arrange
        String reference = "  A1  ";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> CellReferenceResolver.resolveCellReference(reference));
    }
}