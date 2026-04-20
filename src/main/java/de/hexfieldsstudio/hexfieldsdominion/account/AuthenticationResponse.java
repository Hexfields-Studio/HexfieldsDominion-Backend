package de.hexfieldsstudio.hexfieldsdominion.account;

public interface AuthenticationResponse {}

record SuccessAuthenticationResponse (String accessToken) implements AuthenticationResponse {}

record ErrorAuthenticationResponse (String errorMessage) implements AuthenticationResponse {}
