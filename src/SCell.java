public class SCell implements Cell {
    private String line;
    private int type;
    // Add your code here

    public SCell(String s) {
        
        setData(s);
    }

    @Override
    public int getOrder() {


        return 0;
    }

    //@Override
    @Override
    public String toString() {
        return getData();
    }

    @Override
public void setData(String s) {
        // Add your code here
        line = s;
        /////////////////////
    }
    @Override
    public String getData() {
        return line;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int t) {
        type = t;
    }

    @Override
    public void setOrder(int t) {
        // Add your code here

    }
}
