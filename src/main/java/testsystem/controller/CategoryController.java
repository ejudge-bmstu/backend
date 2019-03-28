package testsystem.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import testsystem.dto.CategoryDTO;
import testsystem.dto.CategoryListDTO;
import testsystem.dto.CategoryView;
import testsystem.service.CategoryServiceImpl;

import javax.validation.Valid;

@RestController
public class CategoryController {

    @Autowired
    private CategoryServiceImpl categoryService;

    @PostMapping("/category/add")
    @ResponseStatus(HttpStatus.CREATED)
    @JsonView(CategoryView.ADD.class)
    public void addNewCategory(@RequestBody @Valid CategoryDTO categoryDTO) {
        categoryService.addNewCategory(categoryDTO);
    }

    @GetMapping("/category/list")
    @JsonView(CategoryView.LIST.class)
    public CategoryListDTO getCategoryList() {
        return categoryService.getCategoriesList();
    }
}