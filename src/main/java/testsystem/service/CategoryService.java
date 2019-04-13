package testsystem.service;

import testsystem.dto.CategoryDTO;
import testsystem.dto.CategoryListDTO;

interface CategoryService {

    void addNewCategory(CategoryDTO categoryDTO);

    CategoryListDTO getCategoriesList();

    void editCategory(CategoryDTO categoryDTO);

    void deleteCategory(CategoryDTO categoryDTO);
}
