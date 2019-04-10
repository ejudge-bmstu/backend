package testsystem.integration;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import testsystem.Application;
import testsystem.domain.*;
import testsystem.repository.TaskRepository;
import testsystem.repository.UserSolutionRepository;
import testsystem.service.UserServiceImpl;

import javax.servlet.Filter;
import java.nio.charset.Charset;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@WebAppConfiguration
@Transactional
public class UserControllerTest {
    private static final MediaType APPLICATION_JSON_UTF8 =
            new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserSolutionRepository userSolutionRepository;

    @Autowired
    private TaskRepository taskRepository;

    private MockMvc mvc;

    @Before
    public void init() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();

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
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.is("ext1")));

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
