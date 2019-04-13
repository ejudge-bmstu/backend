package testsystem.exception;

public final class UserAlreadyExistsException extends AppException {

    private final String username;

    public UserAlreadyExistsException(String username) {
        super();
        this.username = username;
    }

    @Override
    public String getMessage() {
        return "Пользователь с именем " + username + " уже существует";
    }
}
