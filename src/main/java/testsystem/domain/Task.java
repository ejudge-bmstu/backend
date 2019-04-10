package testsystem.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class Task {
    @Id
    @GeneratedValue
    private final UUID id = UUID.randomUUID();

    private String name;

    private String description;

    private String report_permission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "task", orphanRemoval = true)
    private List<Limit> limits;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "task", orphanRemoval = true)
    private List<Test> tests;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "task", orphanRemoval = true)
    private List<Example> examples;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "task", orphanRemoval = true)
    private List<UserSolution> solutions;

    public Task(String name, String description, String report_permission, Category category) {
        this.name = name;
        this.description = description;
        this.report_permission = report_permission;
        this.category = category;
        this.limits = new ArrayList<>();
        this.tests = new ArrayList<>();
    }

}
