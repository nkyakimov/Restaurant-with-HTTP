package restaurant.exceptions;

public class UserAlreadyThereException extends RuntimeException {
    public UserAlreadyThereException(String message) {
        super(message);
    }
}
