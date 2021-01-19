package restaurant.intercom;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import restaurant.intercom.orders.UnsentOrder;
import restaurant.storage.Product;
import restaurant.storage.ProductDataBase;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class IntercomTest {
    private static final String RESTAURANT_NAME = "Demo Rest";
    private static final String FILE = "backupFile.txt";
    private static Intercom intercom;
    @Mock
    private static ProductDataBase pdb;

    @BeforeClass
    public static void setUp() throws Exception {
        new FileWriter(FILE,false);
        pdb = Mockito.mock(ProductDataBase.class);
        when(pdb.getProduct("25kl")).thenReturn(new Product("25kl", "ddd", 22., "sushi"));
        when(pdb.getProduct("23kl")).thenReturn(new Product("23kl", "df", 22., "pizza"));
        when(pdb.allMatch("pizza")).thenReturn(List.of(new Product("23kl","",0.,"pizza")));
        intercom = new Intercom(pdb, RESTAURANT_NAME, null, FILE);
        assertTrue(intercom.createTable(55, "nick"));
        assertTrue( intercom.createTable(25, "nick"));
        assertTrue(intercom.createTable(4, "admin"));
    }

    @AfterClass
    public static void tearDown() {
        new File(FILE).deleteOnExit();
    }


    @Test
    public void testOrderAndGetOrders() {
        assertTrue(intercom.order(55, "25kl", "nick", "to go"));
        assertFalse(intercom.order(55, "25kl", "admin", ""));
        assertTrue(intercom.getOrders("sushi").stream().map(UnsentOrder::getProductID)
                .allMatch(orderID -> orderID.equals("25kl")));
    }

    @Test
    public void testAddMessage() {
        intercom.addMessage("nick", "Sushi is ready");
        intercom.addMessage("nick", "Sushi is ready1");
        assertTrue(intercom.getMessages("nick").containsAll(List.of("Sushi is ready1", "Sushi is ready")));
        assertEquals(0, intercom.getMessages("nick").size());
    }

    @Test
    public void testGetTables() {
        assertTrue(intercom.getTables("nick", false).keySet().containsAll(List.of(55, 25)));
        assertTrue(intercom.getTables("nick", true).keySet().containsAll(List.of(55, 25, 4)));
    }

    @Test
    public void testCreateAndDeleteTable() {
        assertTrue(intercom.createTable(23, "nick"));
        assertTrue(intercom.removeTable(23, true));
        assertFalse(intercom.removeTable(23, true));
        assertFalse(intercom.removeTable(25, false));
    }

    @Test
    public void testGetTable() {
        assertNotNull(intercom.getTable("nick", 25, false));
        assertNull(intercom.getTable("", 11, true));
    }

    @Test
    public void testRemoveUser() {
        intercom.removeUser("admin");
        assertNull(intercom.getTable("admin",4,true));
        assertTrue(intercom.createTable(4,"admin"));
    }

    @Test
    public void testAddAndRemoveFromTable() {
        assertTrue(intercom.order(25, "23kl", "nick", ""));
        assertTrue(intercom.removeFromTable(25, "23kl", true));
        assertFalse(intercom.removeFromTable(25, "23kl", false));
        assertFalse(intercom.removeFromTable(25, "23kl", true));
    }

    @Test
    public void testGetRestName() {
        assertEquals(RESTAURANT_NAME, intercom.getRestName());
    }

    @Test
    public void testGetBillPath() {
        assertNull(intercom.getBillPath());
    }


    @Test
    public void testGetProducts() {
        assertTrue(intercom.getProducts("pizza").contains(pdb.getProduct("23kl")));
    }
}