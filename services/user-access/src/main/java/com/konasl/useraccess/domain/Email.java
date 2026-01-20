package com.konasl.useraccess.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing an email address.
 * 
 * BUSINESS RULES:
 * - Email must not be null or blank
 * - Email must be in valid format (contains @ and proper structure)
 * - Email is case-insensitive (stored in lowercase)
 * - Maximum length is 255 characters
 */
public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int MAX_LENGTH = 255;

    public Email {
        Objects.requireNonNull(value, "Email cannot be null");

        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }

        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Email cannot exceed " + MAX_LENGTH + " characters");
        }

        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + trimmed);
        }

        // Store email in lowercase for consistency
        value = trimmed.toLowerCase();
    }

    /**
     * Creates an Email value object from a string.
     *
     * @param value the email string
     * @return Email instance
     * @throws IllegalArgumentException if email is invalid
     */
    public static Email of(String value) {
        return new Email(value);
    }

    /**
     * Returns the email address in lowercase.
     *
     * @return normalized email address
     */
    @Override
    public String value() {
        return value;
    }
}
