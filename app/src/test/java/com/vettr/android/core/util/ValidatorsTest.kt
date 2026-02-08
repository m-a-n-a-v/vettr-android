package com.vettr.android.core.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    // Email validation tests
    @Test
    fun `isValidEmail returns true for valid email addresses`() {
        assertTrue(Validators.isValidEmail("test@example.com"))
        assertTrue(Validators.isValidEmail("user.name@example.com"))
        assertTrue(Validators.isValidEmail("user+tag@example.co.uk"))
        assertTrue(Validators.isValidEmail("test123@subdomain.example.com"))
    }

    @Test
    fun `isValidEmail returns false for invalid email addresses`() {
        assertFalse(Validators.isValidEmail(""))
        assertFalse(Validators.isValidEmail("   "))
        assertFalse(Validators.isValidEmail("invalid"))
        assertFalse(Validators.isValidEmail("invalid@"))
        assertFalse(Validators.isValidEmail("@example.com"))
        assertFalse(Validators.isValidEmail("invalid@.com"))
        assertFalse(Validators.isValidEmail("invalid..email@example.com"))
    }

    // Password validation tests
    @Test
    fun `isValidPassword returns true for valid passwords`() {
        assertTrue(Validators.isValidPassword("Password1"))
        assertTrue(Validators.isValidPassword("MySecure123"))
        assertTrue(Validators.isValidPassword("Test1234"))
        assertTrue(Validators.isValidPassword("UPPERCASE123lowercase"))
        assertTrue(Validators.isValidPassword("Abcdefgh1"))
    }

    @Test
    fun `isValidPassword returns false for passwords shorter than 8 characters`() {
        assertFalse(Validators.isValidPassword("Pass1"))
        assertFalse(Validators.isValidPassword("Abc123"))
        assertFalse(Validators.isValidPassword("Test1"))
    }

    @Test
    fun `isValidPassword returns false for passwords without uppercase letters`() {
        assertFalse(Validators.isValidPassword("password123"))
        assertFalse(Validators.isValidPassword("lowercase1"))
        assertFalse(Validators.isValidPassword("test12345"))
    }

    @Test
    fun `isValidPassword returns false for passwords without digits`() {
        assertFalse(Validators.isValidPassword("Password"))
        assertFalse(Validators.isValidPassword("UPPERCASE"))
        assertFalse(Validators.isValidPassword("MixedCase"))
    }

    @Test
    fun `isValidPassword returns false for empty or blank passwords`() {
        assertFalse(Validators.isValidPassword(""))
        assertFalse(Validators.isValidPassword("       "))
    }

    // Edge case tests
    @Test
    fun `isValidPassword handles passwords with exactly 8 characters`() {
        assertTrue(Validators.isValidPassword("Valid123"))
        assertFalse(Validators.isValidPassword("invalid1"))
        assertFalse(Validators.isValidPassword("INVALID1"))
    }

    @Test
    fun `isValidPassword handles passwords with special characters`() {
        assertTrue(Validators.isValidPassword("P@ssw0rd!"))
        assertTrue(Validators.isValidPassword("My\$ecure1"))
        assertTrue(Validators.isValidPassword("Test#123"))
    }

    @Test
    fun `isValidEmail handles emails with numbers in local part`() {
        assertTrue(Validators.isValidEmail("user123@example.com"))
        assertTrue(Validators.isValidEmail("123user@example.com"))
    }

    @Test
    fun `isValidEmail handles emails with special characters`() {
        assertTrue(Validators.isValidEmail("user.name@example.com"))
        assertTrue(Validators.isValidEmail("user_name@example.com"))
        assertTrue(Validators.isValidEmail("user+tag@example.com"))
    }
}
