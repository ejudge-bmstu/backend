package testsystem.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
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

    @Min(value = 0, message = "Ограничение по времени для языка С должно быть неотрицательно")
    private Integer time_limit_c;

    @Min(value = 0, message = "Ограничение по памяти для языка С должно быть неотрицательно")
    private Integer memory_limit_c;

    @Min(value = 0, message = "Ограничение по времени для языка Python должно быть неотрицательно")
    private Integer time_limit_python;

    @Min(value = 0, message = "Ограничение по памяти для языка Python должно быть неотрицательно")
    private Integer memory_limit_python;

    @Min(value = 0, message = "Ограничение по времени для языка С++ должно быть неотрицательно")
    private Integer time_limit_cpp;

    @Min(value = 0, message = "Ограничение по памяти для языка С должно быть неотрицательно")
    private Integer memory_limit_cpp;

    private String category;

    @NotNull(message = "Доступ к отчету должен быть задан")
    @NotEmpty(message = "Доступ к отчету не должен быть пуст")
    @Pattern(regexp = "full_access|no_access", message = "Возможные значения: full_access, no_access")
    private String access_report;

//    public TaskNewDTO(String name, String description, String category, String access_report) {
//        this.name = name;
//        this.description = description;
//        this.category = category;
//        this.access_report = access_report;
//    }
}
