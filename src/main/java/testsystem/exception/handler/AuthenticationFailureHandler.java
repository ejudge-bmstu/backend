package testsystem.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import testsystem.util.GenericResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
class AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        super.onAuthenticationFailure(request, response, exception);
        request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, exception.getMessage());

        GenericResponse responseBody = new GenericResponse(exception.getMessage(), "AuthenticationError");

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(responseBody);
        response.getWriter().write(body);
    }
}
