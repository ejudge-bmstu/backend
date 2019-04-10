package testsystem.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class TaskNewDTO {

    @NotNull(message = "Название задачи должно быть задано")
    @NotEmpty(message = "Название задачи не должно быть пусто")
    private String name;

    @NotNull(message = "Условие задачи должно быть задано")
    @NotEmpty(message = "Условие задачи не должно быть пусто")
    private String description;

    private Integer time_limit_c;

    private Integer memory_limit_c;

    private Integer time_limit_python;

    private Integer memory_limit_python;

    private Integer time_limit_cpp;

    private Integer memory_limit_cpp;

    @NotNull(message = "Идентификатор категории должен быть задан")
    @NotEmpty(message = "Идентификатор категории не должен быть пуст")
    private String category;

    @NotNull(message = "Доступ к отчету должен быть задан")
    @NotEmpty(message = "Доступ к отчету не должен быть пуст")
    @Pattern(regexp = "full_access|no_access", message = "Возможные значения: full_access, no_access")
    private String access_report;

    public TaskNewDTO(String name, String description, String category, String access_report) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.access_report = access_report;
    }
}
