package restaurant.accounts;

import restaurant.accounts.pair.Pair;
import restaurant.exceptions.AccountDataBaseFileException;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AccountDataBase {
    private final String location;
    private Map<String, Pair> usernameAndPasswords;

    public AccountDataBase(String accountDataBaseLocation) throws AccountDataBaseFileException {
        location = accountDataBaseLocation;
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(location));
            usernameAndPasswords = (ConcurrentHashMap<String, Pair>) ois.readObject();
        } catch (FileNotFoundException | EOFException e) {
            System.out.println("AccountDB loaded with new file");
            usernameAndPasswords = new ConcurrentHashMap<>();
        } catch (ClassNotFoundException | IOException e) {
            throw new AccountDataBaseFileException("Error in reading accountDB file", e);
        }
    }


    public boolean changeStatus(String username) throws AccountDataBaseFileException {
        if (usernameAndPasswords.containsKey(username)) {
            usernameAndPasswords.get(username).shiftAdmin();
            update();
            return true;
        } else {
            return false;
        }
    }

    public boolean changePassword(String username, String oldPassword, String newPassword)
            throws AccountDataBaseFileException {
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

    public boolean addUser(String username, String password, boolean admin) throws AccountDataBaseFileException {
        if (password.indexOf(':') != -1 || usernameAndPasswords.containsKey(username)) {
            return false;
        } else {
            usernameAndPasswords.put(username, new Pair(password, admin));
            update();
            return true;
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

    public synchronized void update() throws AccountDataBaseFileException {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(location));
            oos.writeObject(usernameAndPasswords);
            oos.close();
        } catch (FileNotFoundException e) {
            if (createNewADB(location)) {
                update();
            } else {
                throw new AccountDataBaseFileException("Error when creating accountDb", e);
            }
        } catch (IOException e) {
            throw new AccountDataBaseFileException("Error when updating accountDb", e);
        }
    }

    private boolean createNewADB(String location) {
        File newABD = new File(location);
        try {
            return newABD.createNewFile();
        } catch (IOException e) {
            return false;
        } finally {
            usernameAndPasswords = new ConcurrentHashMap<>();
        }
    }

    public boolean removeUser(String username) throws AccountDataBaseFileException {
        try {
            return usernameAndPasswords.remove(username) != null;
        } finally {
            update();
        }
    }
}
