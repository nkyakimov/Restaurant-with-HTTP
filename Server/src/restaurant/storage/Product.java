package restaurant.storage;

import java.io.Serializable;
import java.util.Objects;

public class Product implements Serializable {
    public static final long serialVersionUID = -2226761754150408384L;
    private final String id;
    private String foodName;
    private Double price;
    private String type;

    public Product(String id, String foodName, Double price, String type) {
        this.id = id;
        this.foodName = foodName;
        this.price = price;
        this.type = type;
    }

    public Product() {
        this(null, null, null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Product product = (Product) o;
        return Objects.equals(id, product.id) &&
                Objects.equals(foodName, product.foodName) &&
                Objects.equals(price, product.price) &&
                Objects.equals(type, product.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toBill() {
        return foodName + " " + getPrice();
    }

    @Override
    public String toString() {
        return "{ \"id\" : \"" + id + "\", \"name\" : \"" + foodName + "\"}";
    }

    public boolean match(String data) {
        final String nameAndID = id + " " + foodName.toLowerCase() + " " + type.toLowerCase();
        for (String word : data.split(" +")) {
            if (!nameAndID.contains(word.toLowerCase())) {
                return false;
            }
        }
        return true;
    }
}
