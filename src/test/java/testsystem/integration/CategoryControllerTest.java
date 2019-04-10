package testsystem.integration;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import testsystem.Application;
import testsystem.domain.Category;
import testsystem.domain.Task;
import testsystem.dto.CategoryDTO;
import testsystem.repository.CategoryRepository;

import javax.servlet.Filter;
import java.util.Arrays;
import java.util.UUID;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@WebAppConfiguration
@Transactional
public class CategoryControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private CategoryRepository categoryRepository;

    private MockMvc mvc;

    @Before
    public void init() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();

        categoryRepository.deleteAll();
    }

    @Test
    public void addCategorySuccess() throws Exception {
        CategoryDTO categoryDTO = new CategoryDTO("name");

        this.mvc.perform(Utils.makePostRequest("/category/add", categoryDTO))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void addCategoryFail() throws Exception {
        categoryRepository.save(new Category("name"));
        CategoryDTO categoryDTO = new CategoryDTO("name");

        this.mvc.perform(Utils.makePostRequest("/category/add", categoryDTO))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.type", Matchers.is("CategoryAlreadyExists")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.is("Категория \"name\" уже существует")));
    }

    @Test
    public void editCategorySuccessNameToNewName() throws Exception {
        Category save = categoryRepository.save(new Category("name"));
        CategoryDTO categoryDTO = new CategoryDTO(save.getId().toString(), "new_name");

        this.mvc.perform(Utils.makePostRequest("/category/edit", categoryDTO))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

        Assert.assertEquals("new_name", categoryRepository.findById(save.getId()).get().getName());
    }

    @Test
    public void editCategorySuccessNameToName() throws Exception {
        Category save = categoryRepository.save(new Category("name"));
        CategoryDTO categoryDTO = new CategoryDTO(save.getId().toString(), "name");

        this.mvc.perform(Utils.makePostRequest("/category/edit", categoryDTO))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

        Assert.assertEquals("name", categoryRepository.findById(save.getId()).get().getName());
    }

    @Test
    public void editCategoryFailConflict() throws Exception {
        categoryRepository.save(new Category("new_name"));
        Category save = categoryRepository.save(new Category("name"));
        CategoryDTO categoryDTO = new CategoryDTO(save.getId().toString(), "new_name");

        this.mvc.perform(Utils.makePostRequest("/category/edit", categoryDTO))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.type", Matchers.is("CategoryAlreadyExists")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.is("Категория \"new_name\" уже существует")));
    }

    @Test
    public void editCategoryFailNoSuch() throws Exception {
        CategoryDTO categoryDTO = new CategoryDTO(UUID.randomUUID().toString(), "new_name");

        this.mvc.perform(Utils.makePostRequest("/category/edit", categoryDTO))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.type", Matchers.is("NoSuchCategory")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.is("Категория не найдена")));
    }

    @Test
    public void deleteCategorySuccess() throws Exception {
        Category save = categoryRepository.save(new Category("name"));
        CategoryDTO categoryDTO = new CategoryDTO(save.getId().toString(), null);

        this.mvc.perform(Utils.makePostRequest("/category/delete", categoryDTO))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

        Assert.assertFalse(categoryRepository.findById(save.getId()).isPresent());
    }

    @Test
    public void deleteCategoryFail() throws Exception {
        CategoryDTO categoryDTO = new CategoryDTO(UUID.randomUUID().toString(), null);

        this.mvc.perform(Utils.makePostRequest("/category/delete", categoryDTO))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.type", Matchers.is("NoSuchCategory")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.is("Категория не найдена")));;

    }

    @Test
    public void getCategoryListEmpty() throws Exception {
        this.mvc.perform(Utils.makeGetRequest("/category/list"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.categories", Matchers.hasSize(0)));
    }

    @Test
    public void getCategoryListNotEmpty() throws Exception {
        categoryRepository.save(new Category("cat1"));

        this.mvc.perform(Utils.makeGetRequest("/category/list"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.categories", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.categories[0].name", Matchers.is("cat1")));
    }

    @Test
    public void getCategoryListWithTasks() throws Exception {
        Category category = new Category("cat1");
        Task task1 = new Task("task1", "desc1", "no_access", category);
        Task task2 = new Task("task2", "desc2", "no_access", category);
        category.setTasks(Arrays.asList(task1, task2));

        Category saved = categoryRepository.save(category);

        this.mvc.perform(Utils.makeGetRequest("/category/list"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.categories", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.categories[0].id", Matchers.is(saved.getId().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.categories[0].name", Matchers.is(saved.getName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.categories[0].count", Matchers.is(2)));
    }

}
