package testsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import testsystem.domain.Category;
import testsystem.dto.CategoryDTO;
import testsystem.dto.CategoryListDTO;
import testsystem.exception.CategoryAlreadyExistsException;
import testsystem.exception.NoSuchCategoryException;
import testsystem.repository.CategoryRepository;
import testsystem.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Override
    public void addNewCategory(CategoryDTO categoryDTO) {
        Category category = Category.fromCategoryDTO(categoryDTO);
        validateCategoryNameExists(category.getName());
        categoryRepository.save(category);
    }

    @Override
    public CategoryListDTO getCategoriesList() {
        List<Category> all = categoryRepository.findAll();

        List<CategoryDTO> list = new ArrayList<>();

        all.forEach(category -> list.add(
                new CategoryDTO(
                        category.getId().toString(),
                        category.getName(),
                        category.getTasks() == null ? 0 : category.getTasks().size()
                )
        ));

        return new CategoryListDTO(list);

    }

    @Override
    public void editCategory(CategoryDTO categoryDTO) {
        UUID uuid = validateId(categoryDTO.getId());
        Category category = validateCategoryExists(uuid);
        if (!category.getName().equals(categoryDTO.getName()))
            validateCategoryNameExists(categoryDTO.getName());

        category.setName(categoryDTO.getName());
        categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(CategoryDTO categoryDTO) {
        UUID uuid = validateId(categoryDTO.getId());
        Category category = validateCategoryExists(uuid);
        taskRepository.findByCategory(category).forEach(task -> {
            task.setCategory(null);
            taskRepository.save(task);
        });
        categoryRepository.delete(category);
    }

    UUID validateId(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new NoSuchCategoryException();
        }
        return uuid;
    }

    Category validateCategoryExists(UUID id) {
        Optional<Category> categoryOptional = categoryRepository.findById(id);
        if (!categoryOptional.isPresent())
            throw new NoSuchCategoryException();
        return categoryOptional.get();
    }

    private void validateCategoryNameExists(String name) {
        if (categoryRepository.findByName(name) != null) {
            throw new CategoryAlreadyExistsException(name);
        }
    }
}
