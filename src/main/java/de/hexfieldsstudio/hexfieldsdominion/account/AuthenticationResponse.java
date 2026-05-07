package de.hexfieldsstudio.hexfieldsdominion.account;

import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationResponse {}

record SuccessAuthenticationResponse (String accessToken) implements AuthenticationResponse {}

record ErrorAuthenticationResponse (String errorMessage, int statusCode) implements AuthenticationResponse {
    public ErrorAuthenticationResponse(String errorMessage) {
        this(errorMessage, HttpServletResponse.SC_BAD_REQUEST);
    }
}
