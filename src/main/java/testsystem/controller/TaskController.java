package testsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import testsystem.dto.TaskDTO;
import testsystem.dto.TaskDescriptionDTO;
import testsystem.dto.TaskListDTO;
import testsystem.dto.TaskNewDTO;
import testsystem.service.TaskServiceImpl;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
public class TaskController {
    @Autowired
    private TaskServiceImpl taskService;

    @GetMapping("/tasks")
    @ResponseStatus(HttpStatus.OK)
    public TaskListDTO getTaskList(@RequestParam(value = "id", defaultValue = "") String id,
                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                   @RequestParam(value = "limit", defaultValue = "12") int limit,
                                   @RequestParam(value = "categorized", defaultValue = "true") boolean categorized) {
        return taskService.getTasksList(id, page, limit, categorized);
    }

    @GetMapping("/task/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskDescriptionDTO getTask(@PathVariable String id) {
        return taskService.getTask(id);
    }

    @PostMapping(value = "/task/add", consumes = {"multipart/form-data"})
    @ResponseStatus(HttpStatus.CREATED)
    @Secured("ROLE_ADMIN")
    public void addTask(@Valid TaskNewDTO taskNewDTO,
                        @RequestParam(value = "inputs[]", defaultValue = "") String[] inputs,
                        @RequestParam(value = "ouputs[]", defaultValue = "") String[] outputs,
                        @RequestParam("tests") @Valid @NotNull(message = "Файл с тестами должен быть задан") MultipartFile file) {
        taskService.addTask(taskNewDTO, inputs, outputs, file);
    }

    @PostMapping(value = "/task/solution", consumes = {"multipart/form-data"})
    @ResponseStatus(HttpStatus.CREATED)
    public void addSolution(@Valid TaskDTO taskDTO,
                            @RequestParam("solution") @Valid @NotNull(message = "Файл с решением должен быть задан")
                                        MultipartFile file) {
        taskService.addSolution(taskDTO, file);
    }
}
