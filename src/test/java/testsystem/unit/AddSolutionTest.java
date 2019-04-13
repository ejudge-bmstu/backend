package testsystem.unit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import testsystem.domain.Answer;
import testsystem.domain.ProgrammingLanguage;
import testsystem.exception.UnknownLanguageException;
import testsystem.service.TaskServiceImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

@RunWith(MockitoJUnitRunner.class)
public class AddSolutionTest {

    @InjectMocks
    private TaskServiceImpl taskService;

    private Method method;

    @Before
    public void init() throws NoSuchMethodException {
        method = TaskServiceImpl.class.getDeclaredMethod("getAnswer", MultipartFile.class);
        method.setAccessible(true);
    }

    @Test
    public void addPyFileSuccess() throws IOException, InvocationTargetException, IllegalAccessException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("ok.py")).getFile());

        FileInputStream fi1 = new FileInputStream(file);
        MockMultipartFile mf = new MockMultipartFile("solution", file.getName(), "multipart/form-data", fi1);

        Answer answer = (Answer) method.invoke(taskService, mf);

        Assert.assertEquals(ProgrammingLanguage.python, answer.getProgramming_language());
        Assert.assertEquals("python file" + System.lineSeparator(), answer.getProgram_text());
    }

    @Test
    public void addCFileSuccess() throws IOException, InvocationTargetException, IllegalAccessException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("ok.c")).getFile());

        FileInputStream fi1 = new FileInputStream(file);
        MockMultipartFile fstmp = new MockMultipartFile("solution", file.getName(), "multipart/form-data", fi1);

        Answer answer = (Answer) method.invoke(taskService, fstmp);

        Assert.assertEquals(ProgrammingLanguage.c, answer.getProgramming_language());
        Assert.assertEquals("c file" + System.lineSeparator(), answer.getProgram_text());
    }

    @Test
    public void addCppFileSuccess() throws IOException, InvocationTargetException, IllegalAccessException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("ok.cpp")).getFile());

        FileInputStream fi1 = new FileInputStream(file);
        MockMultipartFile fstmp = new MockMultipartFile("solution", file.getName(), "multipart/form-data", fi1);

        Answer answer = (Answer) method.invoke(taskService, fstmp);

        Assert.assertEquals(ProgrammingLanguage.cpp, answer.getProgramming_language());
        Assert.assertEquals("cpp file" + System.lineSeparator(), answer.getProgram_text());
    }

    @Test
    public void addFileFail() throws IOException, IllegalAccessException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("invalid_extension.txt")).getFile());

        FileInputStream fi1 = new FileInputStream(file);
        MockMultipartFile fstmp = new MockMultipartFile("solution", file.getName(), "multipart/form-data", fi1);

        try {
            method.invoke(taskService, fstmp);
        } catch (InvocationTargetException e) {
            Assert.assertSame(UnknownLanguageException.class, e.getCause().getClass());
        }

    }

    @Test
    public void addFileFailNoExt() throws IOException, IllegalAccessException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("no_extension")).getFile());

        FileInputStream fi1 = new FileInputStream(file);
        MockMultipartFile fstmp = new MockMultipartFile("solution", file.getName(), "multipart/form-data", fi1);

        try {
            method.invoke(taskService, fstmp);
        } catch (InvocationTargetException e) {
            Assert.assertSame(UnknownLanguageException.class, e.getCause().getClass());
        }

    }
}
