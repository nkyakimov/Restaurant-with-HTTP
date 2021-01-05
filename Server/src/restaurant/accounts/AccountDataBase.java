package restaurant.accounts;

import restaurant.accounts.pair.Pair;
import restaurant.exceptions.CantCreateFile;
import restaurant.exceptions.DataBaseCreationException;
import restaurant.exceptions.UserAlreadyThereException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AccountDataBase {
    private final String location;
    private Map<String, Pair> usernameAndPasswords;

    public AccountDataBase(String accountDataBaseLocation) throws DataBaseCreationException {
        location = accountDataBaseLocation;
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(location));
            usernameAndPasswords = (ConcurrentHashMap<String, Pair>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("AccountDB loaded with new file");
            usernameAndPasswords = new ConcurrentHashMap<>();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    public void print() {
        for (Map.Entry<String, Pair> i : usernameAndPasswords.entrySet()) {
            System.out.println(i.getKey() + " " + i.getValue());
        }
    }

    public boolean changeStatus(String username) {
        if (usernameAndPasswords.containsKey(username)) {
            usernameAndPasswords.get(username).shiftAdmin();
            update();
            return true;
        } else {
            return false;
        }
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        if (oldPassword.indexOf(':') != -1 || newPassword.indexOf(':') != -1) {
            return false;
        }
        try {
            if (!usernameAndPasswords.get(username).verify(oldPassword)) {
                return false;
            }
            usernameAndPasswords
                    .replace(username, new Pair(newPassword, usernameAndPasswords.get(username).getAdmin()));
            update();
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public void addUser(String username, String password, boolean admin) throws UserAlreadyThereException {
        if (password.indexOf(':') != -1) {
            throw new IllegalArgumentException("Password can't contain :");
        }
        if (usernameAndPasswords.get(username) != null) {
            throw new UserAlreadyThereException("User with name " + username + " already registered");
        } else {
            usernameAndPasswords.put(username, new Pair(password, admin));
            update();
        }
    }

    public List<AccountForDevice> getAllAccounts() {
        return usernameAndPasswords.keySet().stream().map(name -> new AccountForDevice(name,
                usernameAndPasswords.get(name))).collect(Collectors.toList());
    }

    public int verify(String username, String password) {
        try {
            Pair pair;
            if ((pair = usernameAndPasswords.get(username)) != null && pair.verify(password)) {
                return pair.getAdmin() ? 1 : 0;
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

    public synchronized void update() {
        try {
            var oos = new ObjectOutputStream(new FileOutputStream(location));
            oos.writeObject(usernameAndPasswords);
            oos.close();
        } catch (FileNotFoundException e) {
            try {
                if (createNewADB(location)) {
                    update();
                }
            } catch (CantCreateFile c) {
                throw new RuntimeException("Error when creating accountDb");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error when updating accountDb");
        }
    }

    private boolean createNewADB(String location) throws CantCreateFile {
        File newABD = new File(location);
        try {
            if (!newABD.createNewFile()) {
                throw new CantCreateFile("Cannot create file for accountDB");
            } else {
                return true;
            }
        } catch (IOException e) {
            return false;
        } finally {
            usernameAndPasswords = new ConcurrentHashMap<>();
        }
    }

    public void clear() {
        usernameAndPasswords.clear();
    }

    public boolean removeUser(String username) {
        try {
            return usernameAndPasswords.remove(username) != null;
        } finally {
            update();
        }
    }
}
