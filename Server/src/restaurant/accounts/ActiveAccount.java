package restaurant.accounts;

import restaurant.table.Table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveAccount implements Serializable {
    private final String username;
    private final Map<Integer, Table> tables;
    private final List<String> notifications;

    public ActiveAccount(String username) {
        this.username = username;
        this.tables = new ConcurrentHashMap<>();
        notifications = new ArrayList<>();
    }

    public void postNotification(String notification) {
        synchronized (notifications) {
            notifications.add(notification);
        }
    }

    public List<String> getNotifications() {
        synchronized (notifications) {
            var answer = new ArrayList<>(notifications);
            notifications.clear();
            return answer;
        }
    }

    public String getUsername() {
        return username;
    }

    public Table getTable(int id) {
        return tables.get(id);
    }

    public Table removeTable(int id) {
        return tables.remove(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return username.equals(((ActiveAccount) o).username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    public Map<Integer, Table> getTables() {
        return tables;
    }

    public void addTable(Table table) {
        tables.put(table.getId(), table);
    }
}
