package com.vettr.android.core.util

import java.util.regex.Pattern

/**
 * InputValidator provides security validation and sanitization for user inputs.
 * Prevents SQL injection, XSS, and other security vulnerabilities.
 *
 * Usage:
 * - Use sanitizeForDisplay() for any user input displayed in UI
 * - Use sanitizeForStorage() for any user input stored in database
 * - Use isValidEmail(), isValidUrl() for specific validation
 */
object InputValidator {

    // Patterns that could be used for SQL injection
    private val SQL_INJECTION_PATTERN = Pattern.compile(
        "('|--|/\\*|\\*/|;|xp_)",
        Pattern.CASE_INSENSITIVE
    )

    // Characters that could be used for XSS attacks
    private val XSS_PATTERN = Pattern.compile(
        "[<>\"'&]",
        Pattern.CASE_INSENSITIVE
    )

    // Email validation pattern (RFC 5322 simplified)
    private val EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    )

    // URL validation pattern
    private val URL_PATTERN = Pattern.compile(
        "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$",
        Pattern.CASE_INSENSITIVE
    )

    // Allowed characters for ticker symbols (letters, numbers, periods, hyphens)
    private val TICKER_PATTERN = Pattern.compile("^[A-Z0-9.-]{1,10}$")

    // Allowed characters for names (letters, spaces, hyphens, apostrophes)
    private val NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s'-]{1,100}$")

    /**
     * Sanitizes input for display in UI to prevent XSS attacks.
     * Escapes HTML special characters.
     *
     * @param input The raw user input
     * @return Sanitized string safe for display
     */
    fun sanitizeForDisplay(input: String?): String {
        if (input.isNullOrBlank()) return ""

        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;")
    }

    /**
     * Sanitizes input for storage in database to prevent SQL injection.
     * Removes potentially dangerous characters.
     *
     * Note: Room uses parameterized queries which already prevent SQL injection,
     * but this provides an additional security layer for defense in depth.
     *
     * @param input The raw user input
     * @return Sanitized string safe for storage
     */
    fun sanitizeForStorage(input: String?): String {
        if (input.isNullOrBlank()) return ""

        // Remove null bytes and control characters
        return input
            .replace("\u0000", "")
            .replace(Regex("[\\x00-\\x1F\\x7F]"), "")
            .trim()
    }

    /**
     * Validates if input contains potential SQL injection patterns.
     *
     * @param input The user input to validate
     * @return true if input appears safe, false if suspicious patterns detected
     */
    fun isSafeSql(input: String?): Boolean {
        if (input.isNullOrBlank()) return true
        return !SQL_INJECTION_PATTERN.matcher(input).find()
    }

    /**
     * Validates if input contains potential XSS patterns.
     *
     * @param input The user input to validate
     * @return true if input appears safe, false if suspicious patterns detected
     */
    fun isSafeXss(input: String?): Boolean {
        if (input.isNullOrBlank()) return true
        return !XSS_PATTERN.matcher(input).find()
    }

    /**
     * Validates email address format.
     *
     * @param email The email address to validate
     * @return true if valid email format, false otherwise
     */
    fun isValidEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        return EMAIL_PATTERN.matcher(email).matches()
    }

    /**
     * Validates URL format.
     *
     * @param url The URL to validate
     * @return true if valid URL format, false otherwise
     */
    fun isValidUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return URL_PATTERN.matcher(url).matches()
    }

    /**
     * Validates ticker symbol format (e.g., "AAPL", "TSLA.V").
     * Allows only uppercase letters, numbers, periods, and hyphens.
     *
     * @param ticker The ticker symbol to validate
     * @return true if valid ticker format, false otherwise
     */
    fun isValidTicker(ticker: String?): Boolean {
        if (ticker.isNullOrBlank()) return false
        return TICKER_PATTERN.matcher(ticker).matches()
    }

    /**
     * Validates person name format.
     * Allows only letters, spaces, hyphens, and apostrophes.
     *
     * @param name The name to validate
     * @return true if valid name format, false otherwise
     */
    fun isValidName(name: String?): Boolean {
        if (name.isNullOrBlank()) return false
        return NAME_PATTERN.matcher(name).matches()
    }

    /**
     * Validates and sanitizes search query input.
     * Removes special characters that could be used for injection.
     *
     * @param query The search query to sanitize
     * @return Sanitized search query
     */
    fun sanitizeSearchQuery(query: String?): String {
        if (query.isNullOrBlank()) return ""

        // Allow alphanumeric, spaces, and basic punctuation
        return query
            .replace(Regex("[^a-zA-Z0-9\\s.,!?'-]"), "")
            .trim()
            .take(200) // Limit length to prevent DoS
    }

    /**
     * Validates numeric input for amounts, scores, etc.
     *
     * @param value The string value to validate
     * @param min Minimum allowed value (inclusive)
     * @param max Maximum allowed value (inclusive)
     * @return true if valid numeric value within range, false otherwise
     */
    fun isValidNumber(value: String?, min: Double = Double.NEGATIVE_INFINITY, max: Double = Double.POSITIVE_INFINITY): Boolean {
        if (value.isNullOrBlank()) return false

        return try {
            val number = value.toDouble()
            number in min..max
        } catch (e: NumberFormatException) {
            false
        }
    }

    /**
     * Checks if input exceeds maximum allowed length.
     *
     * @param input The input to check
     * @param maxLength Maximum allowed length
     * @return true if within length limit, false otherwise
     */
    fun isWithinLength(input: String?, maxLength: Int): Boolean {
        if (input == null) return true
        return input.length <= maxLength
    }

    /**
     * Comprehensive validation for user input fields.
     * Combines multiple validation checks.
     *
     * @param input The input to validate
     * @param maxLength Maximum allowed length
     * @param allowSpecialChars Whether to allow special characters
     * @return ValidationResult with success status and error message
     */
    fun validateInput(
        input: String?,
        maxLength: Int = 1000,
        allowSpecialChars: Boolean = false
    ): ValidationResult {
        if (input.isNullOrBlank()) {
            return ValidationResult(false, "Input cannot be empty")
        }

        if (!isWithinLength(input, maxLength)) {
            return ValidationResult(false, "Input exceeds maximum length of $maxLength characters")
        }

        if (!allowSpecialChars && !isSafeXss(input)) {
            return ValidationResult(false, "Input contains invalid characters")
        }

        if (!isSafeSql(input)) {
            return ValidationResult(false, "Input contains invalid characters")
        }

        return ValidationResult(true, null)
    }

    /**
     * Result of input validation.
     *
     * @property isValid Whether the input passed validation
     * @property errorMessage Error message if validation failed, null otherwise
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String?
    )
}
