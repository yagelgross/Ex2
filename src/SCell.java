// Add your documentation below:

// Add your documentation below:

public class SCell implements Cell {
    private String line; // The content of the cell
    private int type;    // The type of the cell (e.g., TEXT, NUMBER, etc.)
    private int order;   // The natural order of the cell for computation

    /**
     * Constructor initializes the `line` with the given string
     * and sets a default type and order.
     */
    public SCell(String s) {
        setData(s);         // Set the data for the cell
        this.type = Ex2Utils.TEXT; // Default type is TEXT
        this.order = 0;     // Default computation order is 0
    }

    /**
     * Returns the computation order of the cell.
     * The default implementation assumes it's 0.
     */
    @Override
    public int getOrder() {
        return this.order; // Return the current computation order
    }

    /**
     * Sets the order of the cell to the given value.
     */
    @Override
    public void setOrder(int t) {
        this.order = t; // Assign given value as the new order
    }

    /**
     * Overrides `toString` to return the raw data content.
     */
    @Override
    public String toString() {
        return getData();
    }

    /**
     * Sets the content of the cell.
     */
    @Override
    public void setData(String s) {
        this.line = s;      // Update the cell's data
        // Assuming basic logic to determine type based on input
        if (s.isEmpty()) {
            this.type = Ex2Utils.TEXT; // Empty content is treated as text
        } else if (s.matches("-?\\d+(\\.\\d+)?")) { // Matches numeric content
            this.type = Ex2Utils.NUMBER;
        } else if (s.startsWith("=")) { // Starts with `=` suggests formula
            this.type = Ex2Utils.FORM;
        } else {
            this.type = Ex2Utils.TEXT; // Default to TEXT for anything else
        }
    }

    /**
     * Gets the content of the cell.
     */
    @Override
    public String getData() {
        return this.line; // Return the raw data content
    }

    /**
     * Gets the type of the cell (TEXT, NUMBER, FORM, etc.).
     */
    @Override
    public int getType() {
        return this.type; // Return the current type
    }

    /**
     * Sets the type of the cell explicitly.
     */
    @Override
    public void setType(int t) {
        this.type = t; // Assign given type value
    }
}