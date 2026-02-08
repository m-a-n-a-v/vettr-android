package com.vettr.android.core.util

import android.util.Patterns

/**
 * Utility object for validating user input.
 */
object Validators {

    // Email regex pattern as fallback for unit tests
    // Matches standard email format: local-part@domain
    private val EMAIL_REGEX = Regex(
        "^[a-zA-Z0-9][a-zA-Z0-9+._\\-]*@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]*" +
                "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]*)+$"
    )

    /**
     * Validates an email address using Android's Patterns.EMAIL_ADDRESS.
     *
     * @param email The email address to validate
     * @return true if the email is valid, false otherwise
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false

        // Reject emails with consecutive dots (invalid format)
        if (email.contains("..")) return false

        // Try to use Android's Patterns if available, otherwise use regex
        return try {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        } catch (e: Exception) {
            EMAIL_REGEX.matches(email)
        }
    }

    /**
     * Validates a password against security requirements:
     * - Minimum 8 characters
     * - At least 1 uppercase letter
     * - At least 1 lowercase letter
     * - At least 1 digit
     *
     * @param password The password to validate
     * @return true if the password meets requirements, false otherwise
     */
    fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false
        if (!password.any { it.isUpperCase() }) return false
        if (!password.any { it.isLowerCase() }) return false
        if (!password.any { it.isDigit() }) return false
        return true
    }
}
