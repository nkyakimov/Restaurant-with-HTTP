package restaurant.server.handlers;

import restaurant.accounts.ActiveAccount;
import restaurant.server.handlers.orders.UnsentOrder;
import restaurant.storage.Product;
import restaurant.storage.ProductDataBase;
import restaurant.table.Table;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class Intercom implements MainProgram {
    private final Map<String, ActiveAccount> userTables;
    private final ProductDataBase pdb;
    private final String restaurantName;
    private final String billPath;
    private final String backup;
    private final ReadWriteLock unsentOrdersLock = new ReentrantReadWriteLock();
    private Map<Integer, Table> allTables;
    private List<UnsentOrder> unsentOrders;

    public Intercom(ProductDataBase pdb, String restaurantName, String billPath, String backup) {
        unsentOrders = new ArrayList<>();
        allTables = new ConcurrentHashMap<>();
        this.pdb = pdb;
        this.restaurantName = restaurantName;
        this.billPath = billPath;
        this.backup = backup;
        userTables = new ConcurrentHashMap<>();
        restoreBackup();
    }


    private synchronized void backup() {
        try (var oos = new ObjectOutputStream(new FileOutputStream(backup))) {
            oos.writeObject(
                    new ArrayList(userTables.values().stream()
                            .filter(activeAccount -> activeAccount.getTables().size() > 0)
                            .collect(Collectors.toList())));
            try {
                unsentOrdersLock.readLock().lock();
                oos.writeObject(unsentOrders);
            } finally {
                unsentOrdersLock.readLock().unlock();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot perform backup of Server");
        }
    }


    private void restoreBackup() {
        try (var ois = new ObjectInputStream(new FileInputStream(backup))) {
            List<ActiveAccount> accounts = (ArrayList) ois.readObject();
            if (accounts.size() > 0) {
                restoreAccounts(accounts);
            }
            allTables = new ConcurrentHashMap<>(20);
            userTables.values().forEach(account -> allTables.putAll(account.getTables()));
            unsentOrders = (ArrayList) ois.readObject();
            System.out.println("Restore successful");
        } catch (Exception e) {
            unsentOrders = new ArrayList<>();
        }
    }


    private void restoreAccounts(List<ActiveAccount> accounts) {
        for (ActiveAccount i : accounts) {
            userTables.put(i.getUsername(), i);
        }
    }

    @Override
    public List<UnsentOrder> getOrders(String type) {
        try {
            unsentOrdersLock.writeLock().lock();
            var result =
                    unsentOrders.stream()
                            .filter(order -> pdb.getProduct(order.getProductID()).getType().equalsIgnoreCase(type))
                            .collect(Collectors.toList());
            unsentOrders.removeAll(result);
            return result;
        } finally {
            unsentOrdersLock.writeLock().unlock();
            backup();
        }
    }

    @Override
    public void removeUser(String user) {
        try {
            var account = userTables.remove(user);
            var tables = account.getTables();
            for (int i : tables.keySet()) {
                allTables.remove(i);
            }
        } catch (NullPointerException ignored) {

        } finally {
            backup();
        }
    }

    @Override
    public boolean order(int table, String productID, String username, String comment) {
        if (addToTable(table, productID, username)) {
            queueOrder(new UnsentOrder(username, table, productID, comment));
            return true;
        } else {
            return false;
        }
    }

    private void queueOrder(UnsentOrder unsentOrder) {
        try {
            unsentOrdersLock.writeLock().lock();
            unsentOrders.add(unsentOrder);
        } finally {
            unsentOrdersLock.writeLock().unlock();
        }
        backup();
    }


    @Override
    public void addMessage(String username, String message) {
        userTables.get(username).postNotification(message);
        backup();
    }


    @Override
    public Map<Integer, Table> getTables(String username, boolean admin) {
        if (admin) {
            return allTables;
        }
        for (ActiveAccount i : userTables.values()) {
            if (i.getUsername().equals(username)) {
                return i.getTables();
            }
        }
        return new ConcurrentHashMap<>();
    }


    @Override
    public boolean createTable(final int id, final String username) {
        if (allTables.get(id) != null) {
            return false;
        }
        var table = new Table(id);
        allTables.put(id, table);
        if (userTables.containsKey(username)) {
            userTables.get(username).addTable(table);
        } else {
            var account = new ActiveAccount(username);
            account.addTable(table);
            userTables.put(username, account);
        }
        backup();
        return true;
    }


    @Override
    public boolean removeFromTable(int tableId, String productId, boolean admin) {
        if (!admin) {
            return false;
        }
        Table table;
        Product product;
        if ((table = allTables.get(tableId)) != null && (product = pdb.getProduct(productId)) != null) {
            table.removeProduct(product);
            backup();
            return true;
        }
        return false;
    }


    @Override
    public Table getTable(String username, int tableID, boolean admin) {
        if (admin) {
            return allTables.get(tableID);
        }
        if (userTables.containsKey(username)) {
            return userTables.get(username).getTable(tableID);
        } else {
            return null;
        }
    }

    @Override
    public List<String> getMessages(String username) {
        try {
            return userTables.getOrDefault(username, new ActiveAccount("")).getNotifications();
        } finally {
            backup();
        }
    }

    @Override
    public boolean bill(int tableID, String username) {
        try {
            userTables.get(username).removeTable(tableID).bill(getRestName(), getBillPath());
            allTables.remove(tableID);
            backup();
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }


    @Override
    public boolean addToTable(int tableId, String productId, String username) {
        if (userTables.get(username).getTable(tableId) == null) {
            return false;
        }
        Table table;
        Product product;
        if ((table = allTables.get(tableId)) != null && (product = pdb.getProduct(productId)) != null) {
            table.addProduct(product);
            backup();
            return true;
        }
        return false;
    }


    public String getRestName() {
        return restaurantName;
    }


    public String getBillPath() {
        return billPath;
    }


    @Override
    public boolean removeTable(int id, boolean admin) {
        try {
            if (!admin) {
                return false;
            }
            allTables.remove(id);
            for (ActiveAccount account : userTables.values()) {
                if (account.removeTable(id) != null) {
                    return true;
                }
            }
        } finally {
            backup();
        }
        return false;
    }


    @Override
    public List<Product> getProducts(String productInfo) {
        return pdb.allMatch(productInfo);
    }


}