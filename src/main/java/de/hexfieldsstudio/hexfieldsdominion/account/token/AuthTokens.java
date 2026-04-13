package de.hexfieldsstudio.hexfieldsdominion.account.token;


public class AuthTokens {

    public static final int ACCESS_TOKEN_MAX_AGE = 60 * 5; // 5 min

    public static final String REFRESH_TOKEN_NAME = "refreshToken";
    public static final int REFRESH_TOKEN_MAX_AGE = 60 * 60 * 24 * 3; // 3 days

}
