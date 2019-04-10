package testsystem.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import testsystem.domain.User;
import testsystem.dto.EmailTokenDTO;
import testsystem.dto.UserDTO;
import testsystem.dto.UserView;
import testsystem.event.OnRegistrationCompleteEvent;
import testsystem.service.UserServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static testsystem.security.SecurityConstants.SIGN_UP_CONFIRM_URL;
import static testsystem.security.SecurityConstants.SIGN_UP_URL;

@RestController
public class RegistrationController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @PostMapping(SIGN_UP_URL)
    @ResponseStatus(HttpStatus.CREATED)
    public void signUp(@RequestBody @Valid UserDTO user, HttpServletRequest request) {
        User registeredUser = userService.registerUser(user);

        String appUrl = request.getContextPath();
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(
                registeredUser,
                appUrl)
        );
    }

    @PostMapping(SIGN_UP_CONFIRM_URL)
    @JsonView(UserView.UI.class)
    @ResponseStatus(HttpStatus.OK)
    public UserDTO confirmRegistration(@RequestBody @Valid EmailTokenDTO token) {
        return userService.verificateUser(token);
    }

}
