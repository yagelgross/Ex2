import java.io.*;
// Add your documentation below:

public class Ex2Sheet implements Sheet {
    private final Cell[][] table;
    // Add your code here

    // ///////////////////
    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell("");
            }
        }
    }

    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Override
    public String value(int x, int y) {
        if (!isIn(x, y)) {
            return Ex2Utils.EMPTY_CELL;
        }
        Cell c = get(x, y);
        if (c != null) {
            return eval(x, y);
        }
        return Ex2Utils.EMPTY_CELL;
    }

    @Override
    public void eval() {
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                Cell cell = get(i, j);
                if (cell != null && cell.getData().startsWith("=")) {
                    try {
                        String computedValue = eval(i, j);
                        table[i][j] = new SCell(computedValue);
                    } catch (Exception e) {
                        table[i][j] = new SCell("Error");
                    }
                }
            }
        }
    }

    @Override
    public Cell get(int x, int y) {
        if (isIn(x, y)) {
            return table[x][y];
        }
        return null;
    }

    @Override
    public Cell get(String cords) {
        try {
            int col = cords.charAt(0) - 'A';
            int row = Integer.parseInt(cords.substring(1)) - 1;
            if (isIn(col, row)) {
                return get(col, row);
            }
        } catch (Exception e) {
            // Return null if the string format is invalid or out of bounds
        }
        return null;
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
            table[x][y] = new SCell(s);
            eval();
        }
    }

    @Override
    public void evaluate(int[][] dd) {
        if (dd == null || dd.length != width() || dd[0].length != height()) {
            throw new IllegalArgumentException("Invalid dimensions for dd");
        }

        for (int currentDepth = 0; currentDepth <= maxDepth(dd); currentDepth++) {
            for (int x = 0; x < width(); x++) {
                for (int y = 0; y < height(); y++) {
                    if (!isIn(x, y) || x >= dd.length || y >= dd[x].length) continue;

                    Cell cell = get(x, y);

                    if (dd[x][y] == -1) {
                        table[x][y] = new SCell("Error");
                    } else if (dd[x][y] == currentDepth) {
                        if (cell != null && cell.getData().startsWith("=")) {
                            try {
                                String evaluatedValue = eval(x, y);
                                table[x][y] = new SCell(evaluatedValue); // Or reuse
                            } catch (Exception e) {
                                table[x][y] = new SCell("Error");
                            }
                        }
                    }
                }
            }
        }
    }

    private int maxDepth(int[][] dd) {
        int max = 0;
        for (int[] row : dd) {
            for (int d : row) {
                if (d > max) {
                    max = d;
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
                Cell cell = table[i][j];
                if (cell != null) {
                    if (cell.getData().startsWith("=")) {
                        ans[i][j] = 1; // Simplified; replace with more accurate logic for calculating depth.
                    } else {
                        ans[i][j] = 0;
                    }
                }
            }
        }
        return ans;
    }

    @Override
    public void load(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length == 3) {
                    try {
                        int x = Integer.parseInt(parts[0].trim());
                        int y = Integer.parseInt(parts[1].trim());
                        String cellValue = parts[2].trim();
                        if (isIn(x, y)) {
                            set(x, y, cellValue);
                        }
                    } catch (NumberFormatException ignored) {
                        // Skip invalid lines
                    }
                }
            }
        }
    }

    @Override
    public void save(String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("This is an Ex2Spreadsheet file.\n");
            for (int i = 0; i < width(); i++) {
                for (int j = 0; j < height(); j++) {
                    Cell cell = get(i, j);
                    if (cell != null && !cell.getData().isEmpty()) {
                        writer.write(i + "," + j + "," + cell.getData() + "\n");
                    }
                }
            }
        }
    }

    @Override
    public String eval(int x, int y) {
        Cell cell = get(x, y);
        if (cell == null) return Ex2Utils.EMPTY_CELL;
        String data = cell.getData().trim();
        if (data.startsWith("=")) {
            try {
                double result = Double.parseDouble(SCell.computeForm(data));
                return String.valueOf(result);
            } catch (Exception e) {
                return "Error";
            }
        }
        return data;
    }
}
