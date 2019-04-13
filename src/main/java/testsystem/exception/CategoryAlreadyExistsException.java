package testsystem.exception;

public final class CategoryAlreadyExistsException extends AppException {

    private final String name;

    public CategoryAlreadyExistsException(String name) {
        super();
        this.name = name;
    }

    @Override
    public String getMessage() {
        return "Категория \"" + name + "\" уже существует";
    }

}
