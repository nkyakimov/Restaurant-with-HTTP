package restaurant.accounts;

import restaurant.accounts.pair.Pair;

public class AccountForDevice {
    private final String username;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAdmin() {
        return admin;
    }

    private final String password;
    private final boolean admin;

    public AccountForDevice(String name, Pair pair) {
        username = name;
        password = new String(pair.getPassword());
        admin = pair.getAdmin();
    }
}
