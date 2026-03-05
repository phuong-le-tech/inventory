package com.inventory.dto.request;

public final class PasswordConstraints {
    public static final int MIN_LENGTH = 12;
    public static final String MIN_LENGTH_MESSAGE = "Password must be at least 12 characters";

    public static final String LOWERCASE_PATTERN = ".*[a-z].*";
    public static final String LOWERCASE_MESSAGE = "Must contain at least one lowercase letter";

    public static final String UPPERCASE_PATTERN = ".*[A-Z].*";
    public static final String UPPERCASE_MESSAGE = "Must contain at least one uppercase letter";

    public static final String DIGIT_PATTERN = ".*\\d.*";
    public static final String DIGIT_MESSAGE = "Must contain at least one digit";

    public static final String SPECIAL_CHAR_PATTERN = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~`].*";
    public static final String SPECIAL_CHAR_MESSAGE = "Must contain at least one special character (!@#$%^&*...)";

    private PasswordConstraints() {}
}
