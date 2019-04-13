package testsystem.integration;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.request.RequestPartDescriptor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import testsystem.Application;
import testsystem.domain.*;
import testsystem.repository.CategoryRepository;
import testsystem.repository.TaskRepository;
import testsystem.repository.UserSolutionRepository;
import testsystem.service.TaskServiceImpl;
import testsystem.service.TestsystemService;
import testsystem.service.UserServiceImpl;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "build/snippets", uriPort = 3000)
@Transactional
public class TaskControllerTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserSolutionRepository userSolutionRepository;

    @Autowired
    private TaskServiceImpl taskService;

    @Autowired
    private MockMvc mvc;

    private ParameterDescriptor[] taskAddDescr;
    private RequestPartDescriptor[] taskAddFiles;
    private ParameterDescriptor[] taskListParamsDescr;
    private FieldDescriptor[] taskListDescr;
    private FieldDescriptor[] taskDescr;
    private ParameterDescriptor[] taskPath;
    private ParameterDescriptor[] solutionAddDescr;
    private RequestPartDescriptor[] solutionAddFiles;
    //private FieldDescriptor[] errorDescr;
    private FieldDescriptor[] resultsDescr;

    @Before
    public void init() {
        taskListDescr = new FieldDescriptor[] {
                fieldWithPath("total").description("Общее число страниц"),
                fieldWithPath("name").description("Название категории"),
                fieldWithPath("tasks").description("Список задач в категории"),
                fieldWithPath("tasks[].id").description("Идентификатор задачи"),
                fieldWithPath("tasks[].name").description("Название задачи")
        };

        taskListParamsDescr = new ParameterDescriptor[] {
                parameterWithName("id").description("Идентификатор категории"),
                parameterWithName("page").description("Номер страницы, начиная с 0, по умолчанию 0"),
                parameterWithName("limit").description("Размер страницы, по умолчанию 12")
        };

        taskAddDescr = new ParameterDescriptor[] {
                parameterWithName("name").description("Название задачи"),
                parameterWithName("description").description("Условие задачи"),
                parameterWithName("category").description("Идентификатор категории, в которую добавляется задача"),
                parameterWithName("access_report").description("Доступ к отчету (full_access/no_access)"),
                parameterWithName("time_limit_c").description("Ограничение по времени для языка C"),
                parameterWithName("memory_limit_c").description("Ограничение по памяти для языка C"),
                parameterWithName("time_limit_cpp").description("Ограничение по времени для языка C++"),
                parameterWithName("memory_limit_cpp").description("Ограничение по памяти для языка C++"),
                parameterWithName("time_limit_python").description("Ограничение по времени для языка Python"),
                parameterWithName("memory_limit_python").description("Ограничение по памяти для языка Python")
        };

        taskAddFiles = new RequestPartDescriptor[] {
                partWithName("tests").description("Zip-архив с тестами")
        };

        taskDescr = new FieldDescriptor[] {
                fieldWithPath("name").description("Название задачи"),
                fieldWithPath("description").description("Условие задачи"),
                fieldWithPath("access_report").description("Доступ к отчету (full_access/no_access)"),
                fieldWithPath("category.id").description("Идентификатор категории"),
                fieldWithPath("category.name").description("Название категории"),
                fieldWithPath("category.count").description("Число задач в категории"),
                fieldWithPath("languages").description("Список доступных языков"),
                fieldWithPath("languages[].name").description("Название языка"),
                fieldWithPath("limits").description("Список ограничений"),
                fieldWithPath("limits[].language").description("Язык, для которого действует ограничение"),
                fieldWithPath("limits[].memory").description("Ограничение по памяти"),
                fieldWithPath("limits[].time").description("Ограничение по времени"),
                fieldWithPath("examples").description("Примеры входынх и выходных данных"),
                fieldWithPath("examples[].input").description("Входные данные"),
                fieldWithPath("examples[].output").description("Выходные данные")
        };

        taskPath = new ParameterDescriptor[] {
                parameterWithName("id").description("Идентификатор задачи")
        };

        solutionAddDescr = new ParameterDescriptor[] {
                parameterWithName("id").description("Индентификатор решаемой задачи")
        };

        solutionAddFiles = new RequestPartDescriptor[] {
                partWithName("solution").description("Файл с решением, расширение .py/.c/.cpp")
        };

        resultsDescr = new FieldDescriptor[] {
                fieldWithPath("[]").description("Список результатов"),
                fieldWithPath("[].task_name").description("Название задачи"),
                fieldWithPath("[].task_id").description("Идентификатор задачи"),
                fieldWithPath("[].user_name").description("Имя пользователя, решившего задачу"),
                fieldWithPath("[].user_id").description("Идентификатор пользователя, решившего задачу"),
                fieldWithPath("[].total").description("Общее количество тестов"),
                fieldWithPath("[].passed").description("Количество пройденных тестов"),
                fieldWithPath("[].result").description("Результат проверки"),
                fieldWithPath("[].message").description("Отчет об ошибках"),
                fieldWithPath("[].date").description("Дата прохождения")
        };


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
                .andExpect(MockMvcResultMatchers.jsonPath("$.tasks[1].name", Matchers.is("task2")))
                .andDo(Utils.generateDocsGet("task-list", taskListParamsDescr, taskListDescr));
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.tasks[1].name", Matchers.is("task2")))
                .andDo(Utils.generateDocsGet("uncategorized-task-list", null, taskListDescr));
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
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(Utils.generateDocsMultipart("task-add", taskAddDescr, taskAddFiles, null));
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
        task.setExamples(Collections.singletonList(new Example("123", "qwe", null)));
        Task saved = taskRepository.save(task);

        mvc.perform(Utils.makeGetPathRequest("/task/{id}", saved.getId().toString()))
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.limits[2].time",  Matchers.is(6)))
                .andDo(Utils.generateDocsGetPath("task", taskPath, taskDescr));
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
                .andExpect(MockMvcResultMatchers.status().isCreated())
        .andDo(Utils.generateDocsMultipart("solution-add", solutionAddDescr, solutionAddFiles, null));

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
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.is("Задача не найдена")));

    }

    @Test
    public void nonEmptyFullAccessResults() throws Exception {
        User user = userService.findByUsername(Utils.USERNAME);
        Task task = taskRepository.save(new Task("task1", "decs1", "full_access", null));
        Answer answer = new Answer("hello world", ProgrammingLanguage.c);
        Status status = new Status();
        status.setResult("res1");
        status.setExtended_information("ext1");

        UserSolution solution = new UserSolution(System.currentTimeMillis(), user, task, answer, status);
        UserSolution saved = userSolutionRepository.save(solution);
        task.setSolutions(Collections.singletonList(saved));

        this.mvc.perform(Utils.makeGetPathRequest("/task/{id}/results", task.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].task_name", Matchers.is("task1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].task_id", Matchers.is(task.getId().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].user_name", Matchers.is(Utils.USERNAME)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].user_id", Matchers.is(user.getId().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].passed", Matchers.isEmptyOrNullString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].result", Matchers.is("res1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.is("ext1")))
                .andDo(Utils.generateDocsGetPath("task-results", taskPath, resultsDescr));

    }
}
