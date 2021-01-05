package restaurant.exceptions;

public class ProductAlreadyThere extends RuntimeException {
    public ProductAlreadyThere(String message) {
        super(message);
    }
}
