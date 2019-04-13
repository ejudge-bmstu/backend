package testsystem.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.ObjectError;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor(access=AccessLevel.PUBLIC)
public class GenericResponse {

    private String message;
    private String type;

    public GenericResponse(List<ObjectError> allErrors, String error) {
        this.type = error;
        this.message = allErrors
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(System.lineSeparator()));
    }

}
