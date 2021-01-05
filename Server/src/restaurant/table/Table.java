package restaurant.table;

import restaurant.storage.Product;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Table implements Serializable {
    private static final long serialVersionUID = 2506;
    private final int id;
    private final Map<Product, Integer> tableProducts;

    public Table(int id) {
        this.id = id;
        tableProducts = new ConcurrentHashMap<>();
    }

    public Table(int id, Map<Product, Integer> tableProducts) {
        this.id = id;
        this.tableProducts = tableProducts;
    }

    public Map<Product, Integer> getTableProducts() {
        return tableProducts;
    }

    public int getId() {
        return id;
    }

    public void addProduct(Product i) {
        tableProducts.put(i, tableProducts.getOrDefault(i, 0) + 1);
    }

    @Override
    public String toString() {
        return "{" + "id = " + id + ", tableProducts = " + tableProducts + '}';
    }

    /*
        private Map<ProductForDevice, Integer> toDevice() {
            return tableProducts.entrySet().stream().collect(Collectors.toMap(i -> new ProductForDevice(i.getKey()),
                    Map.Entry::getValue));
        }
    */
    public void removeProduct(Product i) {
        int count;
        try {
            if ((count = tableProducts.get(i)) > 1) {
                tableProducts.replace(i, count - 1);
            } else {
                tableProducts.remove(i);
            }
        } catch (NullPointerException e) {
            System.err.println("Cannot remove this product");
        }
    }

    private double getTotal() {
        var total = tableProducts.keySet().stream()
                .mapToDouble(i -> i.getPrice() * tableProducts.get(i))
                .sum();
        BigDecimal bd = BigDecimal.valueOf(total);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void bill(String restaurantName, String filepath) {
        DecimalFormat df = new DecimalFormat("#.##");
        if (Files.notExists(Paths.get(filepath))) {
            try {
                Files.createDirectory(Paths.get(filepath));
            } catch (IOException e) {
                throw new RuntimeException("Cannot create bill directory");
            }
        }
        String billPath = filepath + File.separator
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy  HH_mm"));
        int i = 1;
        while (true) {
            String newPath = billPath + "_(" + i + ").txt";
            File bill = new File(newPath);
            try {
                if (bill.createNewFile()) {
                    FileWriter fw = new FileWriter(newPath, false);
                    fw.append("-- ").append(restaurantName).append(" --").append(System.lineSeparator());
                    tableProducts.keySet().forEach(product -> printProduct(product, fw));
                    fw.append("\t\t Total: ").append(df.format(getTotal())).append(System.lineSeparator());
                    fw.append("---------------------");
                    fw.close();
                    break;
                }
            } catch (IOException e) {
                System.err.println("Cannot create bill");
                return;
            }
            i++;
        }
    }

    private void printProduct(Product product, FileWriter fw) {
        try {
            fw.append(product.toBill())
                    .append(" x")
                    .append(String.valueOf((int) tableProducts.get(product)))
                    .append(System.lineSeparator())
                    .append("\t\t\t")
                    .append(String.valueOf(product.getPrice() * tableProducts.get(product)))
                    .append(System.lineSeparator());
        } catch (IOException ignored) {
        }
    }
}
