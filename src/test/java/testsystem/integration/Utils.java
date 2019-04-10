package testsystem.integration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import testsystem.domain.UserRole;

import java.nio.charset.Charset;

public class Utils {

    public static final MediaType APPLICATION_JSON_UTF8 =
            new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    public static final String USERNAME = "admin";
    public static final String PASSWORD = "123456";

    public static MockHttpServletRequestBuilder makePostRequest(String route, Object body) throws JsonProcessingException {
        return MockMvcRequestBuilders
                .post(route)
                .with(SecurityMockMvcRequestPostProcessors.user(USERNAME)
                        .password(PASSWORD)
                        .roles("ADMIN")
                        .authorities(UserRole.admin))
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .contentType(APPLICATION_JSON_UTF8)
                .content(makeRequestBody(body));
    }

    public static MockHttpServletRequestBuilder makeGetRequest(String route) {
        return MockMvcRequestBuilders
                .get(route)
                .with(SecurityMockMvcRequestPostProcessors.user(USERNAME)
                        .password(PASSWORD)
                        .roles("ADMIN")
                        .authorities(UserRole.admin))
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader());
    }

    public static MockHttpServletRequestBuilder makeMultipartRequest(String route,
                                                                     MockMultipartFile multipartFile,
                                                                     MultiValueMap<String, String> params) {
        return MockMvcRequestBuilders
                .multipart(route)
                .file(multipartFile)
                .params(params)
                .with(SecurityMockMvcRequestPostProcessors.user(USERNAME)
                        .password(PASSWORD)
                        .roles("ADMIN")
                        .authorities(UserRole.admin))
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .contentType("multipart/form-data");
    }

    public static String makeRequestBody(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsString(object);
    }
}
