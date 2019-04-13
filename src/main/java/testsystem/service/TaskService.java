package testsystem.service;

import org.springframework.web.multipart.MultipartFile;
import testsystem.dto.*;

import java.util.List;

interface TaskService {

    TaskListDTO getTasksList(String id, int page, int limit, boolean categorized);

    TaskDescriptionDTO getTask(String id);

    void addTask(TaskNewDTO taskDTO, String[] inputs, String[] outputs, MultipartFile file);

    void addSolution(TaskDTO taskDTO, MultipartFile multipartFile);

    List<ResultDTO> getResults(String id);
}
