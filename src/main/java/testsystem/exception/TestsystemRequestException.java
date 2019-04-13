package testsystem.exception;

public final class TestsystemRequestException extends AppException {

    public TestsystemRequestException() {
        super();
    }

    @Override
    public String getMessage() {
        return "Не удалось отправить решение на проверку";
    }
}
