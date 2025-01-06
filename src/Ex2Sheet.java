import java.io.*;
// Add your documentation below:

public class Ex2Sheet implements Sheet {
    private final Cell[][] table;
    // Add your code here

    // ///////////////////
    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for(int i=0;i<x;i=i+1) {
            for(int j=0;j<y;j=j+1) {
                table[i][j] = new SCell("");
            }
        }
        evaluate();
    }

    private void evaluate() {

    }

    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Override
    public String value(int x, int y) {
        // Default value if the cell is empty or out of bounds
        String ans = Ex2Utils.EMPTY_CELL;

        // Fetch the cell
        Cell c = get(x, y);

        // If the cell is valid, return its evaluated/display value
        if (c != null) {
            ans = eval(x, y) + ""; // Call `eval()` to get the evaluated value (e.g., formula evaluation) and ensure it is a String
        }

        return ans;
    }

    @Override
    public void eval() {
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                Cell cell = get(i, j);
                if (cell != null && cell.toString().startsWith("=")) {
                    String computedValue = eval(i, j) + ""; // Evaluate formula and ensure it is a String
                    set(i, j, computedValue);         // Update the evaluated value in the cell
                }
            }
        }
    }

    @Override
    public Cell get(int x, int y) {
        return table[x][y];
    }

    @Override
    public Cell get(String cords) {
        Cell ans = null;
        // Example: B3 -> column 'B', row 3
        try {
            int col = cords.charAt(0) - 'A'; // Convert letter to column index (A=0, B=1, etc.)
            int row = Integer.parseInt(cords.substring(1)); // Parse the numerical row part
            if (isIn(col, row)) { // Check if the coordinates are valid
                ans = get(col, row);
            }
        } catch (Exception e) {
            // Return null if the string format is invalid or out of bounds
        }
        return ans;
    }

    @Override
    public int width() {
        return table.length;
    }
    @Override
    public int height() {
        return table[0].length;
    }
    @Override
    public void set(int x, int y, String s) {
        if (isIn(x, y)) {
            // Create a new cell with the incoming content
            Cell c = new SCell(s);
            table[x][y] = c;

            // Trigger any necessary updates/evaluations (extend this logic later if more advanced eval logic is needed)
            eval();
        }
    }
    @Override
    public void evaluate(int[][] dd) {

        // Iterate through all depth levels starting from 0
        for (int currentDepth = 0; currentDepth <= maxDepth(dd); currentDepth++) {
            for (int x = 0; x < width(); x++) {
                for (int y = 0; y < height(); y++) {
                    if (dd[x][y] == currentDepth) { // Process only cells at the current depth
                        Cell cell = get(x, y); // Retrieve the cell
                        if (cell != null && cell.toString().startsWith("=")) {
                            // Evaluate formula and update the cell value
                            String evaluatedValue = eval(x, y) + ""; // Use eval() to compute the value and ensure it is a String
                            set(x, y, evaluatedValue); // Replace with the resulting value
                        }
                    } else if (dd[x][y] == -1) {
                        // Handle circular dependencies (formula with depth -1)
                        set(x, y, "Error");
                    }
                }
            }
        }
    }

    // Helper method to find the maximum depth in the depth array
    private int maxDepth(int[][] dd) {
        int max = 0;
        for (int i = 0; i < dd.length; i++) {
            for (int j = 0; j < dd[0].length; j++) {
                if (dd[i][j] > max) {
                    max = dd[i][j];
                }
            }
        }
        return max;
    }

    @Override
    public boolean isIn(int xx, int yy) {
        return xx >= 0 && yy >= 0 && xx < width() && yy < height();
    }

    @Override
    public int[][] depth() {
        int[][] ans = new int[width()][height()];
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (table[i][j] instanceof SCell cell) {
                    if (cell.toString().startsWith("=")) {
                        // Placeholder logic for formula (set all formula cells to depth 1 for now)
                        ans[i][j] = 1;
                    } else {
                        // Static cells have depth 0
                        ans[i][j] = 0;
                    }
                }
            }
        }
        return ans;
    }

    @Override
    public void load(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        while ((line = reader.readLine()) != null) {
            try {
                // Split the line into parts like "x,y,value" (ignore any extra commas/remarks)
                String[] parts = line.split(",", 3);
                if (parts.length >= 3) {
                    int x = Integer.parseInt(parts[0].trim()); // Parse x-coordinate
                    int y = Integer.parseInt(parts[1].trim()); // Parse y-coordinate
                    String cellValue = parts[2].trim();       // The actual cell value (formula, text, number)

                    // Check if (x, y) is a valid cell in the table
                    if (isIn(x, y)) {
                        set(x, y, cellValue); // Set the cell value in the grid
                    }
                }
            } catch (Exception e) {
                // Skip invalid lines silently
            }
        }
        reader.close();
    }

    @Override
    public void save(String fileName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write("This is an Ex2Spreadsheet file.\n"); // Header comment
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                Cell cell = get(i, j);
                if (cell != null && !cell.toString().isEmpty()) { // Write only non-empty cells
                    writer.write(i + "," + j + "," + cell.toString() + "\n");
                }
            }
        }
        writer.close();
    }
    @Override
    public String eval(int x, int y) {
        Cell cell = get(x, y);
        if (cell == null) return Ex2Utils.EMPTY_CELL;
        String data = cell.getData().trim();
        if (data.startsWith("=")) {
            try {
                // Extract formula by removing the starting '='
                String formula = data.substring(1);
    
                // Use Ex2Utils to perform calculation using the formula and current table
                double result = Double.parseDouble(SCell.computeForm(formula));
    
                // Return the result as a String
                return String.valueOf(result);
            } catch (Exception e) {
                // Return an error message for invalid formulas or circular references
                return "Error";
            }
        }
        return data;
    }
}
