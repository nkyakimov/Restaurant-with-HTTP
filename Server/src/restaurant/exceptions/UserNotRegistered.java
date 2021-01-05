package restaurant.exceptions;

public class UserNotRegistered extends Throwable {
    public UserNotRegistered(String message) {
        super(message);
    }
}
