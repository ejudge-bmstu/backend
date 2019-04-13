package testsystem.exception;

public final class NoSuchEmailTokenException extends AppException {

    public NoSuchEmailTokenException() {
        super();
    }

    @Override
    public String getMessage() {
        return "Неизвестный токен верификации";
    }

}
