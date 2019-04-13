package testsystem.exception;

public final class EmailTokenIsExpiredException extends AppException {

    public EmailTokenIsExpiredException() {
        super();
    }

    @Override
    public String getMessage() {
        return "Срок действия токена истек";
    }

}
