package restaurant.storage;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProductDataBaseTest {
    private final static String pdbFile = "demo.txt";
    private final static Product pizza = new Product("25kl", "Pizza Olivetti", 8.99, "Pizza");
    private final static Product sushi = new Product("25kf", "Sushi 1", 18.99, "Sushi");
    private final static Product drink = new Product("25kz", "Coke", 1.99, "Drink");
    private final static Product toDelete = new Product("2511", "No", 13.99, "Drink");
    private static ProductDataBase pdb;

    @BeforeClass
    public static void setUp() throws IOException {
        new FileWriter(pdbFile, false);
        pdb = new ProductDataBase(pdbFile);
        pdb.addProduct(pizza);
        pdb.addProduct(sushi);
        pdb.addProduct(drink);
        pdb.addProduct(toDelete);
    }

    @AfterClass
    public static void tearDown() {
        new File(pdbFile).deleteOnExit();
    }

    @Test
    public void testGetAllTypes() {
        assertTrue(pdb.getAllTypes().containsAll(Arrays.asList("Pizza", "Sushi", "Drink")));
    }

    @Test
    public void testGetProductsByTypes() {
        assertTrue(pdb.getProductsByTypes(List.of("Sushi")).contains(sushi));
    }

    @Test
    public void testGetAllProducts() {
        assertTrue(pdb.getAllProducts().containsAll(List.of(pizza, drink, sushi)));
    }

    @Test
    public void testRemoveProduct() throws IOException {
        assertTrue(pdb.removeProduct(toDelete.getId()));
        assertFalse(pdb.removeProduct("515"));
    }

    @Test
    public void testGetProduct() {
        assertEquals(pizza, pdb.getProduct(pizza.getId()));
    }

    @Test
    public void testAllMatch() {
        assertTrue(pdb.allMatch("pizza").contains(pizza));
    }

    @Test
    public void testChangeProduct() throws IOException {
        assertFalse(pdb.changeProduct(new Product()));
        pizza.setPrice(55.23);
        assertTrue(pdb.changeProduct(pizza));
        assertEquals(55.23,pdb.getProduct(pizza.getId()).getPrice(),0.001);
        pizza.setPrice(8.99);
        assertTrue(pdb.changeProduct(pizza));
    }
}