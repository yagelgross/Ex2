
public class CellEntry  implements Index2D {

    @Override
    public boolean isValid() {
        boolean valid = false;
        if (this.toString() == null)
        {
            return false;
        }
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
        int X = -1;
        if (this.isValid()) {
            for (int i = 0; i < 26; i++) {
                char c = Ex2Utils.ABC[i].charAt(0);
                if (this.toString().charAt(0) == c) {
                    X = i;
                }
                else if (this.toString().charAt(0) == Character.toLowerCase(c))
                {
                    X = i;
                }
            }
        }
    
        return X;
    }


    @Override
    public int getY()
    {
        int Y = -1;
        if (this.isValid())
        {
            Y = Integer.parseInt(this.toString().substring(1));
        }
        return Y;
    }
}
