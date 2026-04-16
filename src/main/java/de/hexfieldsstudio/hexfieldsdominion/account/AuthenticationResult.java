package de.hexfieldsstudio.hexfieldsdominion.account;

import jakarta.servlet.http.Cookie;
import lombok.Builder;

@Builder
public record AuthenticationResult(AuthenticationResponse authenticationResponse, Cookie refreshTokenCookie) {}
