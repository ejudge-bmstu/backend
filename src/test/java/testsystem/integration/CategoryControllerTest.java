package testsystem.integration;

import org.junit.Assert;
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
import org.springframework.transaction.annotation.Transactional;
import testsystem.Application;
import testsystem.domain.Category;
import testsystem.domain.Task;
import testsystem.dto.CategoryDTO;
import testsystem.repository.CategoryRepository;

import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "build/snippets", uriPort = 3000)
@Transactional
public class CategoryControllerTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MockMvc mvc;

    private FieldDescriptor[] categoryAddDescr;
    private FieldDescriptor[] categoryEditDescr;
    private FieldDescriptor[] categoryDeleteDescr;
    private FieldDescriptor[] categoryListDescr;
//    private FieldDescriptor errorDescr[];

    @Before
    public void init() {

        categoryAddDescr = new FieldDescriptor[]{
                fieldWithPath("name").description("Название категории")
        };

        categoryEditDescr = new FieldDescriptor[]{
                fieldWithPath("id").description("Идентификатор категории"),
                fieldWithPath("name").description("Новое название категории")
        };

        categoryDeleteDescr = new FieldDescriptor[]{
                fieldWithPath("id").description("Идентификатор удаляемой категории")
        };

        categoryListDescr = new FieldDescriptor[]{
                fieldWithPath("categories").description("Список категорий"),
                fieldWithPath("categories[].id").description("Идентификатор категории"),
                fieldWithPath("categories[].name").description("Название категории"),
                fieldWithPath("categories[].count").description("Количество задач в категории")
        };

//        errorDescr = new FieldDescriptor[]{
//                fieldWithPath("type").description("Тип ошибки"),
//                fieldWithPath("message").description("Сообщение с информацией об ошибке")
//        };

        categoryRepository.deleteAll();
    }

    @Test
    public void addCategorySuccess() throws Exception {
        CategoryDTO categoryDTO = new CategoryDTO("name");

        this.mvc.perform(Utils.makePostRequest("/category/add", categoryDTO))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(Utils.generateDocsPost("category-add", categoryAddDescr, null));
    }

    @Test
    public void addCategoryFail() throws Exception {
        categoryRepository.save(new Category("name"));
        CategoryDTO categoryDTO = new CategoryDTO("name");

        this.mvc.perform(Utils.makePostRequest("/category/add", categoryDTO))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type", is("CategoryAlreadyExists")))
                .andExpect(jsonPath("$.message", is("Категория \"name\" уже существует")));
    }

    @Test
    public void editCategorySuccessNameToNewName() throws Exception {
        Category save = categoryRepository.save(new Category("name"));
        CategoryDTO categoryDTO = new CategoryDTO(save.getId().toString(), "new_name");

        this.mvc.perform(Utils.makePostRequest("/category/edit", categoryDTO))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(Utils.generateDocsPost("category-edit", categoryEditDescr, null));

        Assert.assertEquals("new_name", categoryRepository.findById(save.getId()).get().getName());
    }

    @Test
    public void editCategorySuccessNameToName() throws Exception {
        Category save = categoryRepository.save(new Category("name"));
        CategoryDTO categoryDTO = new CategoryDTO(save.getId().toString(), "name");

        this.mvc.perform(Utils.makePostRequest("/category/edit", categoryDTO))
                .andDo(print())
                .andExpect(status().isOk());

        Assert.assertEquals("name", categoryRepository.findById(save.getId()).get().getName());
    }

    @Test
    public void editCategoryFailConflict() throws Exception {
        categoryRepository.save(new Category("new_name"));
        Category save = categoryRepository.save(new Category("name"));
        CategoryDTO categoryDTO = new CategoryDTO(save.getId().toString(), "new_name");

        this.mvc.perform(Utils.makePostRequest("/category/edit", categoryDTO))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type", is("CategoryAlreadyExists")))
                .andExpect(jsonPath("$.message", is("Категория \"new_name\" уже существует")));
    }

    @Test
    public void editCategoryFailNoSuch() throws Exception {
        CategoryDTO categoryDTO = new CategoryDTO(UUID.randomUUID().toString(), "new_name");

        this.mvc.perform(Utils.makePostRequest("/category/edit", categoryDTO))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type", is("NoSuchCategory")))
                .andExpect(jsonPath("$.message", is("Категория не найдена")));
    }

    @Test
    public void deleteCategorySuccess() throws Exception {
        Category save = categoryRepository.save(new Category("name"));
        CategoryDTO categoryDTO = new CategoryDTO(save.getId().toString(), null);

        this.mvc.perform(Utils.makePostRequest("/category/delete", categoryDTO))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(Utils.generateDocsPost("category-delete", categoryDeleteDescr, null));

        Assert.assertFalse(categoryRepository.findById(save.getId()).isPresent());
    }

    @Test
    public void deleteCategoryFail() throws Exception {
        CategoryDTO categoryDTO = new CategoryDTO(UUID.randomUUID().toString(), null);

        this.mvc.perform(Utils.makePostRequest("/category/delete", categoryDTO))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type", is("NoSuchCategory")))
                .andExpect(jsonPath("$.message", is("Категория не найдена")));

    }

    @Test
    public void getCategoryListEmpty() throws Exception {
        this.mvc.perform(Utils.makeGetRequest("/category/list"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories", hasSize(0)));
    }

    @Test
    public void getCategoryListNotEmpty() throws Exception {
        categoryRepository.save(new Category("cat1"));

        this.mvc.perform(Utils.makeGetRequest("/category/list"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories", hasSize(1)))
                .andExpect(jsonPath("$.categories[0].name", is("cat1")));
    }

    @Test
    public void getCategoryListWithTasks() throws Exception {
        Category category = new Category("cat1");
        Task task1 = new Task("task1", "desc1", "no_access", category);
        Task task2 = new Task("task2", "desc2", "no_access", category);
        category.setTasks(Arrays.asList(task1, task2));

        Category saved = categoryRepository.save(category);

        this.mvc.perform(Utils.makeGetRequest("/category/list"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories", hasSize(1)))
                .andExpect(jsonPath("$.categories[0].id", is(saved.getId().toString())))
                .andExpect(jsonPath("$.categories[0].name", is(saved.getName())))
                .andExpect(jsonPath("$.categories[0].count", is(2)))
                .andDo(Utils.generateDocsGet("category-list", null, categoryListDescr));
    }

}
