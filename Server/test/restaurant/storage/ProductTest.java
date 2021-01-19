package restaurant.storage;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class ProductTest {

    private final Product product = new Product("25rg", "Somaki", 25.32, "Sushi");

    @Test
    public void testEquals() {
        assertEquals(product, new Product("25rg", null, null, null));
        assertNotEquals(product, new Product("24rg", null, null, null));
    }

    @Test
    public void testGetFoodName() {
        assertEquals("Somaki", product.getFoodName());
    }

    @Test
    public void testGetId() {
        assertEquals("25rg", product.getId());
    }

    @Test
    public void testGetPrice() {
        assertEquals(25.32, product.getPrice(), 0.0);
    }

    @Test
    public void testGetType() {
        assertEquals("Sushi", product.getType());
    }

    @Test
    public void testToString() {
        assertEquals("{ \"id\" : \"25rg\", \"name\" : \"Somaki\"}", product.toString());
    }

    @Test
    public void testMatch() {
        assertTrue(product.match("25"));
        assertTrue(product.match("rg"));
        assertTrue(product.match("Sushi"));
        assertTrue(product.match("Somaki"));
        assertFalse(product.match("Tomato"));
    }
}