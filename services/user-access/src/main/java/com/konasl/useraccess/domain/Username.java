package com.konasl.useraccess.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a username.
 * 
 * BUSINESS RULES:
 * - Username must not be null or blank
 * - Username must be between 3 and 50 characters
 * - Username can only contain alphanumeric characters, underscores, and hyphens
 * - Username must start with an alphanumeric character
 * - Username is case-insensitive (stored in lowercase)
 */
public record Username(String value) {

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9][a-zA-Z0-9_-]*$");
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 50;

    public Username {
        Objects.requireNonNull(value, "Username cannot be null");

        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }

        if (trimmed.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                    "Username must be at least " + MIN_LENGTH + " characters");
        }

        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Username cannot exceed " + MAX_LENGTH + " characters");
        }

        if (!USERNAME_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                    "Username must start with alphanumeric character and can only contain " +
                            "alphanumeric characters, underscores, and hyphens: " + trimmed);
        }

        // Store username in lowercase for consistency
        value = trimmed.toLowerCase();
    }

    /**
     * Creates a Username value object from a string.
     *
     * @param value the username string
     * @return Username instance
     * @throws IllegalArgumentException if username is invalid
     */
    public static Username of(String value) {
        return new Username(value);
    }

    /**
     * Returns the username in lowercase.
     *
     * @return normalized username
     */
    @Override
    public String value() {
        return value;
    }
}
