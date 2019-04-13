package testsystem.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ResultDTO {
    private String task_name;
    private String task_id;
    private String user_name;
    private String user_id;
    private Integer total;
    private Integer passed;
    private String result;
    private String message;
    private Long date;
}
