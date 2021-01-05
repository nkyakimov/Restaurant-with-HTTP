package restaurant.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductDataBase {
    private static final String SUCCESS = "ProductDB successfully loaded";
    private static final String ERROR_UPDATE = "Error updating productDB";
    private final String productDBLocation;
    private final List<String> allTypes;
    private final Map<String, Product> products;

    public ProductDataBase(String productDBLocation) {
        allTypes = new ArrayList<>();
        this.productDBLocation = productDBLocation;
        if ((products = fromJSON()) == null) {
            throw new RuntimeException("Cannot load PDB");
        }
        calculateAllTypes();
        toJSON();
        System.out.println(SUCCESS);
    }

    private void calculateAllTypes() {
        allTypes.addAll(products.values().stream().map(Product::getType).distinct().collect(Collectors.toList()));
    }

    public List<String> getAllTypes() {
        return allTypes;
    }

    public List<Product> getProductsByTypes(List<String> types) {
        if (types.size() == 0) {
            return null;
        }
        return products.values().stream().filter(p -> types.contains(p.getType())).collect(Collectors.toList());
    }

    public Collection<Product> getAllProducts() {
        return products.values();
    }

    public boolean addProduct(Product p) {
        if (products.containsKey(p.getId())) {
            return false;
        } else {
            products.put(p.getId(), p);
            toJSON();
            return true;
        }
    }

    public boolean removeProduct(String id) {
        Product toRemove = products.remove(id);
        if (toRemove == null) {
            return false;
        } else {
            toJSON();
            return true;
        }
    }

    private void toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(new File(productDBLocation),
                    products.values());
        } catch (IOException e) {
            System.err.println(ERROR_UPDATE);
            System.err.println(e.getMessage());
        }
    }

    private Map fromJSON() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File(productDBLocation), new TypeReference<List<Product>>() {
            })
                    .stream().collect(Collectors.toConcurrentMap(Product::getId, product -> product));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    private boolean createDemoProductDB() {
        File file = new File(productDBLocation);
        try {
            if (file.createNewFile()) {
                products.clear();
                var oos = new ObjectOutputStream(new FileOutputStream(productDBLocation));
                oos.writeObject(products);
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            file.deleteOnExit();
            return false;
        }
    }

    public Product getProduct(String i) {
        return products.get(i);
    }

    public List<Product> allMatch(String data) {
        if (data.isBlank()) {
            return new ArrayList<>();
        }
        return products.values().stream()
                .filter(product -> product.match(data))
                .collect(Collectors.toList());
    }

    public void print() {
        products.forEach((k, v) -> System.out.println(k + " " + v.getFoodName() + " " + v.getPrice()));
    }

    public boolean changeProduct(Product product) {
        if (!products.containsKey(product.getId())) {
            return false;
        } else {
            products.replace(product.getId(), product);
            return true;
        }
    }
}
