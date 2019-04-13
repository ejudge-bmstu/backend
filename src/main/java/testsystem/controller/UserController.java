package testsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import testsystem.dto.ResultDTO;
import testsystem.service.UserServiceImpl;

import java.util.List;

@RestController
public class UserController {
    @Autowired
    private UserServiceImpl userService;

    @GetMapping("/user/results")
    @ResponseStatus(HttpStatus.OK)
    public List<ResultDTO> getResultsList() {
        return userService.getResults();
    }
}
