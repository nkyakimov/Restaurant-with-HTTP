package restaurant.table;

import restaurant.storage.Product;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serial;
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
    @Serial
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

    public boolean removeProduct(Product i) {
        int count;
        try {
            if ((count = tableProducts.get(i)) > 1) {
                tableProducts.replace(i, count - 1);
            } else {
                tableProducts.remove(i);
            }
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    private double getTotal() {
        double total = tableProducts.keySet().stream()
                .mapToDouble(i -> i.getPrice() * tableProducts.get(i))
                .sum();
        BigDecimal bd = BigDecimal.valueOf(total);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void bill(String restaurantName, String filepath) {
        DecimalFormat df = new DecimalFormat("#.##");
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd-MM-yy HH_mm");
        checkBillDirectory(filepath);
        String billPath = filepath + File.separator + "table " + id + " " + LocalDateTime.now().format(dateTimeFormat);
        int i = 1;
        while (true) {
            String newPath = billPath + "(" + i + ").txt";
            File bill = new File(newPath);
            try {
                if (bill.createNewFile()) {
                    FileWriter fw = new FileWriter(newPath, false);
                    printBill(fw, restaurantName, df);
                    fw.close();
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            i++;
        }
    }

    private void printBill(FileWriter fw, String restaurantName, DecimalFormat df) throws IOException {
        fw.append("--- ").append(restaurantName).append(" ---").append(System.lineSeparator());
        for (Product product : tableProducts.keySet()) {
            printProduct(product, fw);
        }
        fw.append("\t\t Total: ").append(df.format(getTotal())).append(System.lineSeparator());
        fw.append("---------------------");
    }

    private void checkBillDirectory(String filepath) {
        if (Files.notExists(Paths.get(filepath))) {
            try {
                Files.createDirectory(Paths.get(filepath));
            } catch (IOException e) {
                throw new RuntimeException("Cannot create bill directory", e);
            }
        }
    }

    private void printProduct(Product product, FileWriter fw) throws IOException {
        fw.append(product.toBill()).append(" x").append(String.valueOf((int) tableProducts.get(product)))
                .append(System.lineSeparator()).append("\t\t\t")
                .append(String.valueOf(product.getPrice() * tableProducts.get(product)))
                .append(System.lineSeparator());
    }
}
