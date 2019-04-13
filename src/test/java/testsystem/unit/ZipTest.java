package testsystem.unit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import testsystem.exception.zip.*;
import testsystem.service.TaskServiceImpl;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

@RunWith(MockitoJUnitRunner.class)
public class ZipTest {

    @InjectMocks
    private TaskServiceImpl taskService;

    private Method method;

    @Before
    public void init() throws NoSuchMethodException {
        method = TaskServiceImpl.class.getDeclaredMethod("getTestsFromFile", File.class);
        method.setAccessible(true);
    }

    @Test
    public void notZip() throws IllegalAccessException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("not_zip.txt")).getFile());
        try {
            method.invoke(taskService, file);
        } catch (InvocationTargetException e) {
            Assert.assertSame(OpenFileException.class, e.getCause().getClass());
        }
    }

    @Test
    public void emptyZip() throws IllegalAccessException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("empty.zip")).getFile());
        try {
            method.invoke(taskService, file);
        } catch (InvocationTargetException e) {
            Assert.assertSame(NoOneTestException.class, e.getCause().getClass());
        }
    }

    @Test
    public void invalidFileInZip() throws IllegalAccessException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("invalid_file_in_zip.zip")).getFile());
        try {
            method.invoke(taskService, file);
        } catch (InvocationTargetException e) {
            Assert.assertSame(InvalidFileInZip.class, e.getCause().getClass());
        }
    }

    @Test
    public void invalidFileInDir() throws IllegalAccessException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("invalid_file_in_dir.zip")).getFile());
        try {
            method.invoke(taskService, file);
        } catch (InvocationTargetException e) {
            Assert.assertSame(InvalidFileInDirectory.class, e.getCause().getClass());
        }
    }

    @Test
    public void noIn() throws IllegalAccessException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("no_in.zip")).getFile());
        try {
            method.invoke(taskService, file);
        } catch (InvocationTargetException e) {
            Assert.assertSame(NoInputData.class, e.getCause().getClass());
        }
    }

    @Test
    public void noOut() throws IllegalAccessException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("no_out.zip")).getFile());
        try {
            method.invoke(taskService, file);
        } catch (InvocationTargetException e) {
            Assert.assertSame(NoOutputData.class, e.getCause().getClass());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void success() throws IllegalAccessException, InvocationTargetException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("ok.zip")).getFile());
        List<testsystem.domain.Test> tests = (List<testsystem.domain.Test>) method.invoke(taskService, file);

        String s = "\r\n";
        Assert.assertEquals(2, tests.size());
        Assert.assertEquals("123", tests.get(0).getInput_data());
        Assert.assertEquals("456", tests.get(0).getOutput_data());
        Assert.assertEquals("1" + s + "2" + s + "3" + s, tests.get(1).getInput_data());
        Assert.assertEquals("4" + s + "5" + s + "6", tests.get(1).getOutput_data());
    }
}
