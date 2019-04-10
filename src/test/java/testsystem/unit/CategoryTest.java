package testsystem.unit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import testsystem.Application;
import testsystem.domain.Category;
import testsystem.domain.Task;
import testsystem.repository.CategoryRepository;
import testsystem.repository.TaskRepository;

import java.util.ArrayList;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@Transactional
public class CategoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TaskRepository taskRepository;


    @Test
    public void deleteCategory() {
        Category category = categoryRepository.save(new Category("cat1", new ArrayList<>()));
        Task task = taskRepository.save(new Task("task1", "desc1", "no_access", category));
        categoryRepository.delete(category);

        Assert.assertNotNull(taskRepository.findById(task.getId()));
    }

}
