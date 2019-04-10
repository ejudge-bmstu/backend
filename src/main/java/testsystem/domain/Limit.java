package testsystem.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "task_limits")
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class Limit {

    @Id
    @GeneratedValue
    private final UUID id = UUID.randomUUID();

    private Integer memory_limit;

    private Integer time_limit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @Enumerated(EnumType.STRING)
    private ProgrammingLanguage programming_language;

    public Limit(Integer memory_limit, Integer time_limit, ProgrammingLanguage programming_language) {
        this.memory_limit = memory_limit;
        this.time_limit = time_limit;
        this.programming_language = programming_language;
    }
}
