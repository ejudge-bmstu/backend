package testsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import testsystem.domain.EmailToken;
import testsystem.domain.Profile;
import testsystem.domain.Task;
import testsystem.domain.User;
import testsystem.dto.EmailTokenDTO;
import testsystem.dto.ResultDTO;
import testsystem.dto.UserDTO;
import testsystem.exception.EmailAlreadyExistsException;
import testsystem.exception.EmailTokenIsExpiredException;
import testsystem.exception.NoSuchEmailTokenException;
import testsystem.exception.UserAlreadyExistsException;
import testsystem.repository.EmailTokenRepository;
import testsystem.repository.ProfileRepository;
import testsystem.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private EmailTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(UserDTO userDTO) {
        User user = User.fromUserDTO(userDTO);

        String username = user.getUsername();
        String email = user.getEmail();

        if (userRepository.findByUsername(username) != null)
            throw new UserAlreadyExistsException(username);
        if (userRepository.findByEmail(email) != null)
            throw new EmailAlreadyExistsException(email);

        user.setPassword_hash(passwordEncoder.encode(user.getPassword_hash()));

        Profile profile = new Profile();
        Profile saved = profileRepository.save(profile);
        user.setProfile(saved);

        return userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public UserDTO verificateUser(EmailTokenDTO token) {
        EmailToken verificationToken = this.getVerificationToken(token.getToken());
        if (verificationToken == null) {
            throw new NoSuchEmailTokenException();
        }

        if (verificationToken.isTokenExpired()) {
            throw new EmailTokenIsExpiredException();
        }

        User user = verificationToken.getUser();
        this.activateUser(user);

        return UserDTO.userWithoutEmailAndWithoutToken(
                user.getUsername(),
                user.getRole().toString()
        );
    }

    private boolean emailExist(String email) {
        User user = userRepository.findByEmail(email);
        return user != null;
    }


    private EmailToken getVerificationToken(String token) {
        return tokenRepository.findByToken(token);
    }

    @Override
    public void createEmailToken(User user, String token) {
        EmailToken emailToken = new EmailToken(token, user);
        tokenRepository.save(emailToken);
    }

    @Override
    public void activateUser(User user) {
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public List<ResultDTO> getResults() {
        List<ResultDTO> results = new ArrayList<>();

        User user = getCurrentUser();
        if (user.getSolutions() != null) {
            user.getSolutions().forEach(userSolution -> {
                Task task = userSolution.getTask();
                results.add(new ResultDTO(
                        task.getName(),
                        task.getId().toString(),
                        task.getTests().size(),
                        userSolution.getStatus().getPassed(),
                        userSolution.getStatus().getResult(),
                        task.getReport_permission().equals("full_access") ?
                                userSolution.getStatus().getExtended_information() : null,
                        userSolution.getSolution_date()));
            });
        }

        return results;
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByUsername(username);
    }
}
