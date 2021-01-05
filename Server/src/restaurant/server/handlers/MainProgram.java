package restaurant.server.handlers;

import restaurant.server.handlers.orders.UnsentOrder;
import restaurant.storage.Product;
import restaurant.table.Table;

import java.util.List;
import java.util.Map;

public interface MainProgram {
    boolean createTable(final int tableID, final String username);

    boolean removeTable(final int tableID, boolean admin);

    boolean bill(int tableID, String username);

    boolean removeFromTable(int tableID, String productID, boolean admin);

    boolean addToTable(int tableID, String productID, String username);

    boolean order(int tableID, String productID, String username, String comment);

    List<Product> getProducts(String productInfo);

    void addMessage(String username, String message);

    Map<Integer, Table> getTables(String username, boolean admin);

    Table getTable(String username, int tableID, boolean admin);

    List<String> getMessages(String username);

    List<UnsentOrder> getOrders(String type);

    void removeUser(String user);
}
