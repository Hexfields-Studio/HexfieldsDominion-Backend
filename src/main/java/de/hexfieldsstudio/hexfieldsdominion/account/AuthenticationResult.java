package de.hexfieldsstudio.hexfieldsdominion.account;

import jakarta.servlet.http.Cookie;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthenticationResult {

    private AuthenticationResponse authenticationResponse;
    private Cookie refreshTokenCookie;

}
