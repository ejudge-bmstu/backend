package testsystem.exception.zip;

public final class NoInputData extends ZipFileException {

    private final String dirname;

    public NoInputData(String dirname) { this.dirname = dirname; }

    @Override
    public String getMessage() {
        return "В директории " + dirname + " отсутствует файл in.txt";
    }

}
