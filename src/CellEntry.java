// Add your documentation below:

public class CellEntry  implements Index2D {

    @Override
    public boolean isValid() {
        boolean valid = false;
        if (this.toString().matches("[A-Z][0-9]+"))
        {
            valid = true;
        }
        else if (this.toString().matches("[a-z][0-9]+"))
        {
            valid = true;
        }
        if (this.toString().length()>3)
        {
            valid = false;
        }
        return valid;
    }

    @Override
    public int getX() 
    {
        if (this.isValid()) {
            for (int i = 0; i < 26; i++) {
                char c = Ex2Utils.ABC[i].charAt(0);
                if (this.toString().charAt(0) == c) {
                    return i;
                }
            }
        }
    
        return -1; // Return -1 if no match is found
    }


    @Override
    public int getY()
    {
        return Integer.parseInt(this.toString().substring(1));
    }
}
