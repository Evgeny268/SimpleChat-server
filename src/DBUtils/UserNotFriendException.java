package DBUtils;

public class UserNotFriendException extends Exception {
    public UserNotFriendException() {
        super();
    }

    public UserNotFriendException(String message) {
        super(message);
    }
}
