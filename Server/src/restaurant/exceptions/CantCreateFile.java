package restaurant.exceptions;

public class CantCreateFile extends RuntimeException {
    public CantCreateFile(String message) {
        super(message);
    }
}
