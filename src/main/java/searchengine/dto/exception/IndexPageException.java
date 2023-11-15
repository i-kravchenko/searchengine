package searchengine.dto.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IndexPageException extends RuntimeException
{
    private String message;
    private HttpStatus code;
}
