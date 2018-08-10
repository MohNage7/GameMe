package com.gameme.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    /**
     * Validate username with regular expression
     *
     * @param username username for validation
     * @return true valid username, false invalid username
     */
    public static boolean validateUsername(final String username) {
        final String USERNAME_PATTERN = "^[a-z0-9_-]{3,10}$";
        Pattern pattern = Pattern.compile(USERNAME_PATTERN);
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();

    }

    public static boolean isPasswordValid(String password) {
        return password.length() > 6;
    }

    public static boolean validateEmail(String emailStr) {
        if (emailStr == null) {
            return false;
        }
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }
}
