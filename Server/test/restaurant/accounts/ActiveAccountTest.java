package restaurant.accounts;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import restaurant.table.Table;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ActiveAccountTest {
    private static ActiveAccount activeAccount;

    @BeforeClass
    public static void setUp() {
        activeAccount = new ActiveAccount("nick");
        activeAccount.postNotification("Sushi ready");
        activeAccount.addTable(new Table(5));
    }

    @AfterClass
    public static void testRemoveTable() {
        assertNotNull(activeAccount.removeTable(5));
        assertNotNull(activeAccount.removeTable(55));
    }

    @Test
    public void testGetNotifications() {
        assertTrue(activeAccount.getNotifications().contains("Sushi ready"));
        assertEquals(activeAccount.getNotifications().size(), 0);
    }

    @Test
    public void testGetUsername() {
        assertEquals("nick", activeAccount.getUsername());
    }

    @Test
    public void testGetTable() {
        assertNotNull(activeAccount.getTable(5));
        assertNull(activeAccount.getTable(6));
    }

    @Test
    public void testEquals() {
        assertEquals(activeAccount, new ActiveAccount("nick"));
    }

    @Test
    public void testConcurrency() throws InterruptedException {
        ActiveAccount account = new ActiveAccount("demo");
        account.postNotification("New Sushi");
        account.postNotification("New Salmon");
        AtomicInteger count = new AtomicInteger();
        Thread test1 = new Thread(() -> {
            List<String> notifications = account.getNotifications();
            count.addAndGet(notifications.size());
            assertTrue(
                    notifications.containsAll(List.of("New Sushi", "New Salmon")) || notifications.size() == 0);
        });
        Thread test2 = new Thread(() -> {
            List<String> notifications = account.getNotifications();
            count.addAndGet(notifications.size());
            assertTrue(
                    notifications.containsAll(List.of("New Sushi", "New Salmon")) || notifications.size() == 0);
        });
        test1.start();
        test2.start();
        Thread.sleep(500);
        assertEquals(2, count.get());
    }

    @Test
    public void testGetTables() {
        activeAccount.addTable(new Table(55));
        Map<Integer, Table> tableMap = activeAccount.getTables();
        assertTrue(tableMap.size() == 2 && tableMap.containsKey(5) && tableMap.containsKey(55));
    }

}