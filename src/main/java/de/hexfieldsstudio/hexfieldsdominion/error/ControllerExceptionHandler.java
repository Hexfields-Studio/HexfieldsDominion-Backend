package de.hexfieldsstudio.hexfieldsdominion.error;

import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleNotFound(Exception exception) {
        return responseOf(HttpServletResponse.SC_NOT_FOUND, exception);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleForbidden(Exception exception) {
        return responseOf(HttpServletResponse.SC_FORBIDDEN, exception);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleBadRequest(Exception exception) {
        return responseOf(HttpServletResponse.SC_BAD_REQUEST, exception);
    }

    private ResponseEntity<@NonNull ErrorResponse> responseOf(int status, Exception exception) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(exception));
    }

    public record ErrorResponse(String errorMessage) {
        public ErrorResponse(Exception exception) {
            this(exception.getMessage());
        }
    }

}
