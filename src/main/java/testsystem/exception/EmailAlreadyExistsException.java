package testsystem.exception;

public final class EmailAlreadyExistsException extends AppException {

    private final String email;

    public EmailAlreadyExistsException(String email) {
        super();
        this.email = email;
    }

    @Override
    public String getMessage() {
        return "Электронная почта " + email + " уже зарегистрирована";
    }

}
