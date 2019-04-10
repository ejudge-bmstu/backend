package testsystem.integration;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import testsystem.Application;
import testsystem.domain.Category;
import testsystem.domain.Limit;
import testsystem.domain.ProgrammingLanguage;
import testsystem.domain.Task;
import testsystem.repository.CategoryRepository;
import testsystem.repository.TaskRepository;
import testsystem.service.TaskServiceImpl;
import testsystem.service.TestsystemService;

import javax.servlet.Filter;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@WebAppConfiguration
@Transactional
public class TaskControllerTest {

    private static final MediaType APPLICATION_JSON_UTF8 =
            new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TaskServiceImpl taskService;

    private MockMvc mvc;

    @Before
    public void init() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();

        categoryRepository.deleteAll();
        taskRepository.deleteAll();

    }

    @Test
    public void getTasksListByCategoryFail() throws Exception {
        this.mvc.perform(Utils.makeGetRequest("/tasks?id=" + UUID.randomUUID()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.type", Matchers.is("NoSuchCategory")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.is("Категория не найдена")));
    }

    @Test
    public void getTasksListByCategoryEmpty() throws Exception {
        Category category = new Category("cat1");
        Category saved = categoryRepository.save(category);

        this.mvc.perform(Utils.makeGetRequest("/tasks?id=" + saved.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.total", Matchers.is(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is("cat1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.tasks", Matchers.hasSize(0)));
    }

    @Test
    public void getTasksListByCategoryIdPage0Limit2() throws Exception {
        Category category = new Category("cat1");
        Task task1 = new Task("task1", "desc1", "no_access", category);
        Task task2 = new Task("task2", "desc2", "no_access", category);
        category.setTasks(Arrays.asList(task1, task2));

        Category saved = categoryRepository.save(category);

        this.mvc.perform(Utils.makeGetRequest("/tasks?id=" + saved.getId() + "&page=0&limit=2"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.total", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is("cat1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.tasks", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.tasks[0].name", Matchers.is("task1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.tasks[1].name", Matchers.is("task2")));
    }

    @Test
    public void getTasksListByCategoryIdPage0Limit1() throws Exception {
        Category category = new Category("cat1");
        Task task1 = new Task("task1", "desc1", "no_access", category);
        Task task2 = new Task("task2", "desc2", "no_access", category);
        category.setTasks(Arrays.asList(task1, task2));

        Category saved = categoryRepository.save(category);

        this.mvc.perform(Utils.makeGetRequest("/tasks?id=" + saved.getId() + "&page=0&limit=1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.total", Matchers.is(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is("cat1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.tasks", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.tasks[0].name", Matchers.is("task1")));
    }

    @Test
    public void getUncategorizedTasksList() throws Exception {
        taskRepository.save(new Task("task1", "desc1", "no_access", null));
        taskRepository.save(new Task("task2", "desc2", "no_access", null));

        this.mvc.perform(Utils.makeGetRequest("/tasks?categorized=false"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.total", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.nullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.tasks", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.tasks[0].name", Matchers.is("task1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.tasks[1].name", Matchers.is("task2")));
    }

    @Test
    public void addTaskWithoutLimitsSuccess() throws Exception {
        Category category = categoryRepository.save(new Category("name"));

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("ok.zip")).getFile());

        FileInputStream fi1 = new FileInputStream(file);
        MockMultipartFile fstmp = new MockMultipartFile("tests", file.getName(), "multipart/form-data", fi1);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", "task1");
        params.add("description", "decs1");
        params.add("category", category.getId().toString());
        params.add("access_report", "no_access");

        mvc.perform(Utils.makeMultipartRequest("/task/add", fstmp, params))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void addTaskWithLimitsSuccess() throws Exception {
        Category category = categoryRepository.save(new Category("name"));

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("ok.zip")).getFile());

        FileInputStream fi1 = new FileInputStream(file);
        MockMultipartFile fstmp = new MockMultipartFile("tests", file.getName(), "multipart/form-data", fi1);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", "task1");
        params.add("description", "decs1");
        params.add("category", category.getId().toString());
        params.add("access_report", "no_access");
        params.add("time_limit_c", "1");
        params.add("memory_limit_c", "2");
        params.add("time_limit_cpp", "3");
        params.add("memory_limit_cpp", "4");
        params.add("time_limit_python", "5");
        params.add("memory_limit_python", "6");

        mvc.perform(Utils.makeMultipartRequest("/task/add", fstmp, params))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void addTaskFail() throws Exception {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("ok.zip")).getFile());

        FileInputStream fi1 = new FileInputStream(file);
        MockMultipartFile fstmp = new MockMultipartFile("tests", file.getName(), "multipart/form-data", fi1);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", "task1");
        params.add("description", "decs1");
        params.add("category", UUID.randomUUID().toString());
        params.add("access_report", "no_access");

        mvc.perform(Utils.makeMultipartRequest("/task/add", fstmp, params))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.type", Matchers.is("NoSuchCategory")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.is("Категория не найдена")));
    }

    @Test
    public void getTaskWithoutLimitsSuccess() throws Exception {
        Category category = categoryRepository.save(new Category("name"));
        Task task = taskRepository.save(new Task("task1", "desc1", "no_access", category));

        mvc.perform(Utils.makeGetRequest("/task/" + task.getId().toString()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name",  Matchers.is("task1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description",  Matchers.is("desc1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.access_report",  Matchers.is("no_access")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.languages",  Matchers.hasSize(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.languages[0].name",  Matchers.is("python")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.languages[1].name",  Matchers.is("c")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.languages[2].name",  Matchers.is("c++")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.limits",  Matchers.hasSize(0)));
    }

    @Test
    public void getTaskWithLimitsSuccess() throws Exception {
        Category category = categoryRepository.save(new Category("name"));
        Task task = new Task("task1", "desc1", "no_access", category);
        Limit limit1 = new Limit(1,2, ProgrammingLanguage.python);
        Limit limit2 = new Limit(3,4, ProgrammingLanguage.c);
        Limit limit3 = new Limit(5,6, ProgrammingLanguage.cpp);
        task.setLimits(Arrays.asList(limit1, limit2, limit3));
        Task saved = taskRepository.save(task);

        mvc.perform(Utils.makeGetRequest("/task/" + saved.getId().toString()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.limits",  Matchers.hasSize(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.limits[0].language",  Matchers.is("python")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.limits[0].memory",  Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.limits[0].time",  Matchers.is(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.limits[1].language",  Matchers.is("c")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.limits[1].memory",  Matchers.is(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.limits[1].time",  Matchers.is(4)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.limits[2].language",  Matchers.is("c++")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.limits[2].memory",  Matchers.is(5)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.limits[2].time",  Matchers.is(6)));
    }

    @Test
    public void getTaskFail() throws Exception {

        mvc.perform(Utils.makeGetRequest("/task/" + UUID.randomUUID().toString()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.type", Matchers.is("NoSuchTask")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.is("Задача не найдена")));
    }

    @Test
    public void sendSolutionSuccess() throws Exception {
        TestsystemService mock = Mockito.mock(TestsystemService.class);
        Mockito.when(mock.sendRequestToTestingServer(Mockito.anyString())).thenReturn(200);

        taskService.setTestsystemService(mock);

        Task task = taskRepository.save(new Task("task1", "decs1", "no_access", null));

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("ok.py")).getFile());

        FileInputStream fi1 = new FileInputStream(file);
        MockMultipartFile fstmp = new MockMultipartFile("solution", file.getName(), "multipart/form-data", fi1);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", task.getId().toString());

        mvc.perform(Utils.makeMultipartRequest("/task/solution", fstmp, params))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated());

    }

    @Test
    public void sendSolutionFail() throws Exception {
        TestsystemService mock = Mockito.mock(TestsystemService.class);
        Mockito.when(mock.sendRequestToTestingServer(Mockito.anyString())).thenReturn(200);

        taskService.setTestsystemService(mock);

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("ok.py")).getFile());

        FileInputStream fi1 = new FileInputStream(file);
        MockMultipartFile fstmp = new MockMultipartFile("solution", file.getName(), "multipart/form-data", fi1);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", UUID.randomUUID().toString());

        mvc.perform(Utils.makeMultipartRequest("/task/solution", fstmp, params))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.type", Matchers.is("NoSuchTask")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.is("Задача не найдена")));;

    }
}
