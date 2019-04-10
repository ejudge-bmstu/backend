package testsystem.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class CategoryDTO {

    @NotNull(message = "Идентификатор категории должен быть задан",
             groups = {CategoryGroup.DELETE.class, CategoryGroup.EDIT.class})
    @NotEmpty(message = "Идентификатор категории не должен быть пуст",
              groups = {CategoryGroup.DELETE.class, CategoryGroup.EDIT.class})
    private String id;

    @NotNull(message = "Название категории должно быть задано",
             groups = {CategoryGroup.ADD.class, CategoryGroup.EDIT.class})
    @NotEmpty(message = "Название категории не должно быть пусто",
              groups = {CategoryGroup.ADD.class, CategoryGroup.EDIT.class})
    private String name;

    private Integer count;

    public CategoryDTO(String name) {
        this.name = name;
    }

    public CategoryDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }

}
