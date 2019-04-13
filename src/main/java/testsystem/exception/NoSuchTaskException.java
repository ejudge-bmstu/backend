package testsystem.exception;

public final class NoSuchTaskException extends AppException {

    public NoSuchTaskException() {
        super();
    }

    @Override
    public String getMessage() {
        return "Задача не найдена";
    }

}
