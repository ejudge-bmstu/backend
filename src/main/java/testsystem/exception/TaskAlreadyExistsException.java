package testsystem.exception;

public final class TaskAlreadyExistsException extends AppException {

    private final String taskName;
    private final String categoryName;

    public TaskAlreadyExistsException(String taskName, String categoryName) {
        super();
        this.taskName = taskName;
        this.categoryName = categoryName;
    }

    @Override
    public String getMessage() {
        return "Задача \"" + taskName + "\" уже существует в категории \"" + categoryName + "\"";
    }

}
