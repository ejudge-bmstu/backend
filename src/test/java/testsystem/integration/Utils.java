package testsystem.integration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.request.RequestPartDescriptor;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import testsystem.domain.UserRole;

import java.nio.charset.Charset;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;

class Utils {

    private static final MediaType APPLICATION_JSON_UTF8 =
            new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    static final String USERNAME = "admin";
    private static final String PASSWORD = "123456";

    private static final String[] headersRequest = new String[]{
            "X-CSRF-TOKEN", "Content-Length"
    };

    private static final String[] headersResponse = new String[]{
            "Pragma", "X-XSS-Protection", "Expires", "X-Frame-Options",
            "X-Content-Type-Options", "Cache-Control", "Content-Length"
    };

    static MockHttpServletRequestBuilder makePostRequest(String route, Object body) throws JsonProcessingException {
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

    static MockHttpServletRequestBuilder makeGetRequest(String route) {
        return MockMvcRequestBuilders
                .get(route)
                .with(SecurityMockMvcRequestPostProcessors.user(USERNAME)
                        .password(PASSWORD)
                        .roles("ADMIN")
                        .authorities(UserRole.admin))
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader());
    }

    static MockHttpServletRequestBuilder makeGetPathRequest(String route, Object... args) {
        return RestDocumentationRequestBuilders
                .get(route, args)
                .with(SecurityMockMvcRequestPostProcessors.user(USERNAME)
                        .password(PASSWORD)
                        .roles("ADMIN")
                        .authorities(UserRole.admin))
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader());
    }

    static MockHttpServletRequestBuilder makeMultipartRequest(String route,
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

    private static String makeRequestBody(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsString(object);
    }

    static RestDocumentationResultHandler generateDocsPost(String id, FieldDescriptor[] request, FieldDescriptor[] response) {
        if (request != null && response != null)
            return document(
                    id,
                    preprocessRequest(removeHeaders(headersRequest), prettyPrint()),
                    preprocessResponse(removeHeaders(headersResponse), prettyPrint()),
                    requestFields(request),
                    responseFields(response));
        if (request != null && response == null)
            return document(
                    id,
                    preprocessRequest(removeHeaders(headersRequest), prettyPrint()),
                    preprocessResponse(removeHeaders(headersResponse), prettyPrint()),
                    requestFields(request));
        if (request == null && response != null)
            return document(
                    id,
                    preprocessRequest(removeHeaders(headersRequest), prettyPrint()),
                    preprocessResponse(removeHeaders(headersResponse), prettyPrint()),
                    responseFields(response));

        return document(
                    id,
                    preprocessRequest(removeHeaders(headersRequest), prettyPrint()),
                    preprocessResponse(removeHeaders(headersResponse), prettyPrint()));
    }

    static RestDocumentationResultHandler generateDocsGet(String id, ParameterDescriptor[] request, FieldDescriptor[] response) {
        if (request != null && response != null)
            return document(
                    id,
                    preprocessRequest(removeHeaders(headersRequest), prettyPrint()),
                    preprocessResponse(removeHeaders(headersResponse), prettyPrint()),
                    requestParameters(request),
                    responseFields(response));
        if (request != null && response == null)
            return document(
                    id,
                    preprocessRequest(removeHeaders(headersRequest), prettyPrint()),
                    preprocessResponse(removeHeaders(headersResponse), prettyPrint()),
                    requestParameters(request));
        if (request == null && response != null)
            return document(
                    id,
                    preprocessRequest(removeHeaders(headersRequest), prettyPrint()),
                    preprocessResponse(removeHeaders(headersResponse), prettyPrint()),
                    responseFields(response));

        return document(
                id,
                preprocessRequest(removeHeaders(headersRequest), prettyPrint()),
                preprocessResponse(removeHeaders(headersResponse), prettyPrint()));
    }

    static RestDocumentationResultHandler generateDocsGetPath(String id, ParameterDescriptor[] request, FieldDescriptor[] response) {
        if (request != null && response != null)
            return document(
                    id,
                    preprocessRequest(removeHeaders(headersRequest), prettyPrint()),
                    preprocessResponse(removeHeaders(headersResponse), prettyPrint()),
                    pathParameters(request),
                    responseFields(response));
        if (request != null && response == null)
            return document(
                    id,
                    preprocessRequest(removeHeaders(headersRequest), prettyPrint()),
                    preprocessResponse(removeHeaders(headersResponse), prettyPrint()),
                    pathParameters(request));
        if (request == null && response != null)
            return document(
                    id,
                    preprocessRequest(removeHeaders(headersRequest), prettyPrint()),
                    preprocessResponse(removeHeaders(headersResponse), prettyPrint()),
                    responseFields(response));

        return document(
                id,
                preprocessRequest(removeHeaders(headersRequest), prettyPrint()),
                preprocessResponse(removeHeaders(headersResponse), prettyPrint()));
    }

    static RestDocumentationResultHandler generateDocsMultipart(String id, ParameterDescriptor[] request, RequestPartDescriptor[] files, FieldDescriptor[] response) {
        if (request != null && response != null)
            return document(
                    id,
                    preprocessRequest(removeHeaders(headersRequest), prettyPrint()),
                    preprocessResponse(removeHeaders(headersResponse), prettyPrint()),
                    requestParameters(request),
                    requestParts(files),
                    responseFields(response));
        if (request != null && response == null)
            return document(
                    id,
                    preprocessRequest(removeHeaders(headersRequest), prettyPrint()),
                    preprocessResponse(removeHeaders(headersResponse), prettyPrint()),
                    requestParameters(request));
        if (request == null && response != null)
            return document(
                    id,
                    preprocessRequest(removeHeaders(headersRequest), prettyPrint()),
                    preprocessResponse(removeHeaders(headersResponse), prettyPrint()),
                    responseFields(response));

        return document(
                id,
                preprocessRequest(removeHeaders(headersRequest), prettyPrint()),
                preprocessResponse(removeHeaders(headersResponse), prettyPrint()));
    }
}
