package restaurant.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import restaurant.exceptions.ProductDataBaseFileException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class ProductDataBase {
    private static final String SUCCESS = "ProductDB successfully loaded";
    private final String productDBLocation;
    private final Map<String, Product> products;
    private Set<String> allTypes;

    public ProductDataBase(String productDBLocation) {
        allTypes = new ConcurrentSkipListSet<>();
        this.productDBLocation = productDBLocation;
        Map<String, Product> productMap = new ConcurrentHashMap<>();
        try {
            if ((productMap = fromJSON()) == null) {
                throw new RuntimeException("Cannot load PDB");
            } else {
                System.out.println(SUCCESS);
            }
        }finally {
            products = productMap;
        }
        calculateAllTypes();
    }

    private void calculateAllTypes() {
        allTypes =
                products.values().stream().map(Product::getType)
                        .collect(Collectors.toCollection(ConcurrentSkipListSet::new));
    }

    public Collection<String> getAllTypes() {
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
        try {
            if (products.containsKey(p.getId())) {
                return false;
            } else {
                products.put(p.getId(), p);
                return true;
            }
        } finally {
            toJSON();
            calculateAllTypes();
        }
    }

    public boolean removeProduct(String id) {
        try {
            return products.remove(id) != null;
        } finally {
            toJSON();
            calculateAllTypes();
        }
    }

    private void toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            mapper.writeValue(new File(productDBLocation), products.values());
        } catch (IOException e) {
            throw new ProductDataBaseFileException("Error writing file", e);
        }
    }

    private Map fromJSON() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File(productDBLocation), new TypeReference<List<Product>>() {
            })
                    .stream().collect(Collectors.toConcurrentMap(Product::getId, product -> product));
        }catch (MismatchedInputException e) {
            return new ConcurrentHashMap();
        }catch (IOException e) {
            throw new ProductDataBaseFileException("Error reading file", e);
        }
    }

    public Product getProduct(String id) {
        return products.get(id);
    }

    public List<Product> allMatch(String data) {
        if (data.isBlank()) {
            return new ArrayList<>();
        }
        return products.values().stream()
                .filter(product -> product.match(data))
                .collect(Collectors.toList());
    }

    public boolean changeProduct(Product product) {
        try {
            if (!products.containsKey(product.getId())) {
                return false;
            } else {
                products.replace(product.getId(), product);
                return true;
            }
        } finally {
            toJSON();
            calculateAllTypes();
        }
    }
}
