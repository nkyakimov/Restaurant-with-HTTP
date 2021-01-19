package restaurant.table;

import org.junit.Before;
import org.junit.Test;
import restaurant.storage.Product;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TableTest {
    private final static Product pizza = new Product("25kl", "Pizza Olivetti", 8.99, "Pizza");
    private final static Product sushi = new Product("25kf", "Sushi 1", 18.99, "Sushi");
    private final static Product drink = new Product("25kz", "Coke", 1.99, "Drink");
    private static Table table;

    @Before
    public void setUp() throws Exception {
        table = new Table(5);
        table.addProduct(pizza);
        table.addProduct(pizza);
        table.addProduct(pizza);
        table.addProduct(sushi);
    }

    @Test
    public void testGetTableProducts() {
        assertEquals(Integer.valueOf(3), table.getTableProducts().get(pizza));
        assertEquals(Integer.valueOf(1), table.getTableProducts().get(sushi));
    }

    @Test
    public void testGetId() {
        assertEquals(5, table.getId());
    }

    @Test
    public void testAddProduct() {
        table.addProduct(drink);
        assertEquals(Integer.valueOf(1), table.getTableProducts().get(drink));
    }

    @Test
    public void testRemoveProduct() {
        if (table.getTableProducts().get(drink) == null) {
            table.addProduct(drink);
        }
        table.removeProduct(drink);
        assertNull(table.getTableProducts().get(drink));
    }

}