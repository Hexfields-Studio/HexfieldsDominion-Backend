package de.hexfieldsstudio.hexfieldsdominion.error;

import de.hexfieldsstudio.hexfieldsdominion.account.error.InvalidCharactersException;
import de.hexfieldsstudio.hexfieldsdominion.account.error.UserAlreadyExistsException;
import de.hexfieldsstudio.hexfieldsdominion.game.error.MatchNotFoundException;
import de.hexfieldsstudio.hexfieldsdominion.lobby.error.LobbyNotFoundException;
import de.hexfieldsstudio.hexfieldsdominion.lobby.error.NotOwnerOfLobbyException;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ControllerExceptionHandlerTest {

    @InjectMocks
    private ControllerExceptionHandler controllerExceptionHandler;

    @ParameterizedTest
    @ArgumentsSource(NotFoundExceptionsProvider.class)
    public void testHandleNotFound(Exception exception) {
        ResponseEntity<ControllerExceptionHandler.@NonNull ErrorResponse> response = controllerExceptionHandler.handleNotFound(exception);

        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode().value());
    }

    @ParameterizedTest
    @ArgumentsSource(ForbiddenExceptionsProvider.class)
    public void testHandleForbidden(Exception exception) {
        ResponseEntity<ControllerExceptionHandler.@NonNull ErrorResponse> response = controllerExceptionHandler.handleForbidden(exception);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusCode().value());
    }

    @ParameterizedTest
    @ArgumentsSource(BadRequestExceptionsProvider.class)
    public void testHandleBadRequest(Exception exception) {
        ResponseEntity<ControllerExceptionHandler.@NonNull ErrorResponse> response = controllerExceptionHandler.handleBadRequest(exception);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatusCode().value());
    }

    static class NotFoundExceptionsProvider implements ArgumentsProvider {
        @Override
        @NullMarked
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                    Arguments.of(new NotFoundException("")),
                    Arguments.of(new LobbyNotFoundException("")),
                    Arguments.of(new MatchNotFoundException(UUID.randomUUID()))
            );
        }
    }

    static class ForbiddenExceptionsProvider implements ArgumentsProvider {
        @Override
        @NullMarked
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                    Arguments.of(new ForbiddenException("")),
                    Arguments.of(new NotOwnerOfLobbyException())
            );
        }
    }

    static class BadRequestExceptionsProvider implements ArgumentsProvider {
        @Override
        @NullMarked
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                    Arguments.of(new BadRequestException("")),
                    Arguments.of(new InvalidCharactersException()),
                    Arguments.of(new InvalidCharactersException()),
                    Arguments.of(new UserAlreadyExistsException())
            );
        }
    }

}
