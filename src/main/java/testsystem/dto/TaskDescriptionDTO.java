package testsystem.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class TaskDescriptionDTO {
    private String name;
    private String description;
    private String access_report;
    private CategoryDTO category;
    private List<LanguageDTO> languages;
    private List<LimitDTO> limits;
    private List<ExampleDTO> examples;
}
