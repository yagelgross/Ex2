import java.util.*;

public class SCell implements Cell {
    private String content; // Raw content of the cell
    private int type; // Cell type (e.g., TEXT, NUMBER, FORM, etc.)
    private int order; // Computation order (used for dependency depth)
    private final Set<String> dependencies = new HashSet<>(); // Dependencies to other cells

    public SCell(String content) {
        this.content = content;
        setType(Ex2Utils.getType(content)); // Determine type of content
        parseAndSetDependencies(); // Parse dependencies if formula
    }

    @Override
    public String getData() {
        return content;
    }

    @Override
    public void setData(String s) {
        this.content = s;
        setType(Ex2Utils.getType(s)); // Update type when setting data
        parseAndSetDependencies();
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int t) {
        this.type = t;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int t) {
        this.order = t;
    }

    public boolean isFormula() {
        return type == Ex2Utils.FORM;
    }

    public int computeForm(Ex2Sheet sheet) {
        try {
            if (!isFormula()) return Integer.parseInt(content);
            return Ex2Utils.evaluateFormula(content.substring(1), sheet);
        } catch (StackOverflowError e) {
            return Ex2Utils.ERR_CYCLE_FORM; // Handle circular dependencies
        } catch (Exception e) {
            return Ex2Utils.ERR_WRONG_FORM;
        }
    }

    private void parseAndSetDependencies() {
        dependencies.clear();
        if (isFormula()) {
            String formula = content.substring(1); // Strip '='
            dependencies.addAll(Ex2Utils.extractCellReferences(formula)); // Extract cell references
        }
    }

    public Set<String> getDependencies() {
        return dependencies;
    }
}