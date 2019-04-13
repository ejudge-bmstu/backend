package testsystem.exception;

public final class UnknownLanguageException extends AppException {

    public UnknownLanguageException() {
        super();
    }

    @Override
    public String getMessage() {
        return "Не удалось определить язык программирования по расширению файла";
    }
}
