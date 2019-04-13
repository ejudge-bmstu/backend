package testsystem.service;

import org.springframework.web.multipart.MultipartFile;
import testsystem.domain.Task;
import testsystem.dto.*;

import java.util.List;

public interface TaskService {

    TaskListDTO getTasksList(String id, int page, int limit, boolean categorized);

    TaskDescriptionDTO getTask(String id);

    Task addTask(TaskNewDTO taskDTO, String[] inputs, String[] outputs, MultipartFile file);

    void addSolution(TaskDTO taskDTO, MultipartFile multipartFile);

    List<ResultDTO> getResults(String id);
}
