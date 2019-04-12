package testsystem.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import testsystem.domain.User;
import testsystem.event.OnRegistrationCompleteEvent;
import testsystem.service.UserService;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import static testsystem.security.SecurityConstants.SIGN_UP_CONFIRM_URL;

@Service
@EnableAsync
public class RegistrationListener {

    private UserService service;

    private Sender sender;

    private String host;

    private String port;

    private Properties mailProp;

    private static final String SUBJECT = "Подтверждение регистрации";

    private static final String MESSAGE = "Для подтверждения регистрации перейдите по ссылке: ";

    @Autowired
    public RegistrationListener(UserService service, Environment env) {
        this.service = service;

        this.host = env.getProperty("frontend.address");
        this.port = env.getProperty("frontend.port");

        mailProp = new Properties();
        configurateMailSender();
    }

    @Async
    @EventListener
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        service.createEmailToken(user, token);

        String confirmationUrl = event.getAppUrl() + SIGN_UP_CONFIRM_URL + "?token=" + token;
        String messageFull = MESSAGE + String.format("http://%s:%s%s", host, port, confirmationUrl);

        if (sender != null)
            sender.send(SUBJECT, messageFull, user.getEmail());
    }

    private void configurateMailSender() {

        try {
            mailProp.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("mail.properties"));
            sender = new Sender(mailProp.getProperty("mail.username"), mailProp.getProperty("mail.password"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    class Sender {

        private String username;
        private Session session;

        Sender(String username, String password) {
            this.username = username;
            session = Session.getInstance(mailProp, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        }

        void send(String subject, String text, String toEmail) {

            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject(subject);
                message.setText(text);

                Transport.send(message);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
