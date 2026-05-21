package de.hexfieldsstudio.hexfieldsdominion;

import de.hexfieldsstudio.hexfieldsdominion.lobby.error.LobbyNotFoundException;
import de.hexfieldsstudio.hexfieldsdominion.game.error.MatchNotFoundException;
import de.hexfieldsstudio.hexfieldsdominion.lobby.error.NotOwnerOfLobbyException;
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

    @ExceptionHandler({
            LobbyNotFoundException.class,
            MatchNotFoundException.class
    })
    public ResponseEntity<@NonNull String> handleNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(NotOwnerOfLobbyException.class)
    public ResponseEntity<@NonNull String> handleForbidden() {
        return ResponseEntity.status(403).build();
    }

}
