package testsystem.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import testsystem.dto.CategoryDTO;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "categories")
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class Category {
    @Id
    @GeneratedValue
    private final UUID id = UUID.randomUUID();

    private String name;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id")
    private List<Task> tasks;

    public static Category fromCategoryDTO(CategoryDTO categoryDTO) {
        return new Category(categoryDTO.getName(), new ArrayList<>());
    }

    public Category(String name) {
        this.name = name;
        this.tasks = new ArrayList<>();
    }
}
