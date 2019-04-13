package testsystem.service;

import testsystem.domain.User;
import testsystem.dto.EmailTokenDTO;
import testsystem.dto.ResultDTO;
import testsystem.dto.UserDTO;

import java.util.List;

public interface UserService {

    User registerUser(UserDTO userDTO);

    User findByUsername(String username);

    UserDTO verificateUser(EmailTokenDTO token);

    void createEmailToken(User user, String token);

    List<ResultDTO> getResults();
}
