package restaurant.accounts.pair;

import java.io.Serializable;
import java.util.Arrays;

public class Pair implements Serializable {
    private final static long serialVersionUID = 7762809784240109250L;

    public char[] getPassword() {
        return password;
    }

    private final char[] password;
    private Boolean admin;

    public Pair(String password, Boolean admin) {
        this.password = password.toCharArray();
        this.admin = admin;
    }

    @Override
    public String toString() {
        return "password = " + new String(password) + " admin = " + admin;
    }

    public boolean verify(String password) {
        return Arrays.equals(this.password, password.toCharArray());
    }

    public boolean getAdmin() {
        return admin;
    }

    public void shiftAdmin() {
        admin = !admin;
    }
}
