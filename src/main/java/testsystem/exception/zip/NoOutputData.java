package testsystem.exception.zip;

public final class NoOutputData extends ZipFileException {

    private final String dirname;

    public NoOutputData(String dirname) { this.dirname = dirname; }

    @Override
    public String getMessage() {
        return "В директории " + dirname + " отсутствует файл out.txt";
    }

}
