package de.hexfieldsstudio.hexfieldsdominion;

import de.hexfieldsstudio.hexfieldsdominion.account.*;
import de.hexfieldsstudio.hexfieldsdominion.account.dto.LoginDTO;
import de.hexfieldsstudio.hexfieldsdominion.account.dto.RegisterDTO;
import de.hexfieldsstudio.hexfieldsdominion.account.token.SseTokenService;
import de.hexfieldsstudio.hexfieldsdominion.account.user.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RootControllerTest {

    @InjectMocks
    private RootController rootController;

    @Test
    void testRoot() {
        assertEquals("OK", rootController.root());
    }

}
