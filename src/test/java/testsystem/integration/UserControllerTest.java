package testsystem.integration;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import testsystem.Application;
import testsystem.domain.*;
import testsystem.repository.TaskRepository;
import testsystem.repository.UserSolutionRepository;
import testsystem.service.UserServiceImpl;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "build/snippets", uriPort = 3000)
@Transactional
public class UserControllerTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserSolutionRepository userSolutionRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MockMvc mvc;

    private FieldDescriptor[] resultsDescr;

    @Before
    public void init() {

        resultsDescr = new FieldDescriptor[] {
                fieldWithPath("[]").description("Список результатов"),
                fieldWithPath("[].name").description("Название задачи"),
                fieldWithPath("[].id").description("Идентификатор задачи"),
                fieldWithPath("[].total").description("Общее количество тестов"),
                fieldWithPath("[].passed").description("Количество пройденных тестов"),
                fieldWithPath("[].result").description("Результат проверки"),
                fieldWithPath("[].message").description("Отчет об ошибках"),
                fieldWithPath("[].date").description("Дата прохождения")
        };

        userSolutionRepository.findAll().forEach(s -> s.getUser().getSolutions().clear());
    }

    @Test
    public void emptyResults() throws Exception {
        this.mvc.perform(Utils.makeGetRequest("/user/solutions"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0)));

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
        user.getSolutions().add(saved);

        this.mvc.perform(Utils.makeGetRequest("/user/solutions"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is("task1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(task.getId().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].passed", Matchers.isEmptyOrNullString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].result", Matchers.is("res1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.is("ext1")))
                .andDo(Utils.generateDocsPost("results", null, resultsDescr));

    }

    @Test
    public void nonEmptyNoAccessResults() throws Exception {
        User user = userService.findByUsername(Utils.USERNAME);
        Task task = taskRepository.save(new Task("task1", "decs1", "no_access", null));
        Answer answer = new Answer("hello world", ProgrammingLanguage.c);
        Status status = new Status();
        status.setResult("res1");
        status.setExtended_information("ext1");

        UserSolution solution = new UserSolution(System.currentTimeMillis(), user, task, answer, status);
        UserSolution saved = userSolutionRepository.save(solution);
        user.getSolutions().add(saved);

        this.mvc.perform(Utils.makeGetRequest("/user/solutions"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is("task1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(task.getId().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].passed", Matchers.isEmptyOrNullString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].result", Matchers.is("res1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.isEmptyOrNullString()));

    }
}
