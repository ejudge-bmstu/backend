package testsystem.event;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import testsystem.domain.User;
import testsystem.service.UserServiceImpl;

@Getter
public class OnRegistrationCompleteEvent extends ApplicationEvent {
    private final String appUrl;
    private final User user;

    @Autowired
    UserServiceImpl service;

    public OnRegistrationCompleteEvent(User user, String appUrl) {
        super(user);

        this.user = user;
        this.appUrl = appUrl;
    }

}
