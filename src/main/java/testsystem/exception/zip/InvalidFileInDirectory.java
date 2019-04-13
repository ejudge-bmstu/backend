package testsystem.exception.zip;

public final class InvalidFileInDirectory extends ZipFileException {

    private final String dirname;

    public InvalidFileInDirectory(String dirname) { this.dirname = dirname; }

    @Override
    public String getMessage() {
        return "В директории " + dirname + " содержатся недопустимые файлы";
    }

}
