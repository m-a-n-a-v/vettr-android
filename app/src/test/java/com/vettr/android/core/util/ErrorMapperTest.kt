package com.vettr.android.core.util

import com.google.gson.JsonParseException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Unit tests for ErrorMapper utility.
 */
class ErrorMapperTest {

    @Test
    fun `map UnknownHostException returns NetworkError`() {
        val exception = UnknownHostException("Unable to resolve host")
        val result = ErrorMapper.map(exception)

        assertTrue(result is AppError.NetworkError)
        assertEquals(
            "Unable to connect to the server. Please check your internet connection.",
            result.userMessage
        )
        assertEquals(SuggestedAction.Retry, result.suggestedAction)
    }

    @Test
    fun `map SocketTimeoutException returns NetworkError`() {
        val exception = SocketTimeoutException("timeout")
        val result = ErrorMapper.map(exception)

        assertTrue(result is AppError.NetworkError)
        assertEquals(
            "The request timed out. Please check your connection and try again.",
            result.userMessage
        )
        assertEquals(SuggestedAction.Retry, result.suggestedAction)
    }

    @Test
    fun `map IOException returns NetworkError`() {
        val exception = IOException("Network error")
        val result = ErrorMapper.map(exception)

        assertTrue(result is AppError.NetworkError)
        assertEquals(
            "A network error occurred. Please check your connection and try again.",
            result.userMessage
        )
        assertEquals(SuggestedAction.Retry, result.suggestedAction)
    }

    @Test
    fun `map JsonParseException returns InvalidData`() {
        val exception = JsonParseException("Malformed JSON")
        val result = ErrorMapper.map(exception)

        assertTrue(result is AppError.InvalidData)
        assertEquals(
            "Unable to process the server response. Please try again later.",
            result.userMessage
        )
        assertEquals(SuggestedAction.Retry, result.suggestedAction)
    }

    @Test
    fun `map 400 HttpException returns InvalidData`() {
        val exception = HttpException(Response.error<Any>(400, okhttp3.ResponseBody.create(null, "")))
        val result = ErrorMapper.map(exception)

        assertTrue(result is AppError.InvalidData)
        assertEquals(
            "Invalid request. Please check your input and try again.",
            result.userMessage
        )
        assertEquals(SuggestedAction.Retry, result.suggestedAction)
    }

    @Test
    fun `map 401 HttpException returns Unauthorized`() {
        val exception = HttpException(Response.error<Any>(401, okhttp3.ResponseBody.create(null, "")))
        val result = ErrorMapper.map(exception)

        assertTrue(result is AppError.Unauthorized)
        assertEquals(
            "You are not authorized. Please sign in again.",
            result.userMessage
        )
        assertEquals(SuggestedAction.ContactSupport, result.suggestedAction)
    }

    @Test
    fun `map 403 HttpException returns Unauthorized`() {
        val exception = HttpException(Response.error<Any>(403, okhttp3.ResponseBody.create(null, "")))
        val result = ErrorMapper.map(exception)

        assertTrue(result is AppError.Unauthorized)
        assertEquals(
            "Access denied. You don't have permission to access this resource.",
            result.userMessage
        )
        assertEquals(SuggestedAction.ContactSupport, result.suggestedAction)
    }

    @Test
    fun `map 404 HttpException returns NotFound`() {
        val exception = HttpException(Response.error<Any>(404, okhttp3.ResponseBody.create(null, "")))
        val result = ErrorMapper.map(exception)

        assertTrue(result is AppError.NotFound)
        assertEquals(
            "The requested resource was not found.",
            result.userMessage
        )
        assertEquals(SuggestedAction.GoHome, result.suggestedAction)
    }

    @Test
    fun `map 408 HttpException returns NetworkError`() {
        val exception = HttpException(Response.error<Any>(408, okhttp3.ResponseBody.create(null, "")))
        val result = ErrorMapper.map(exception)

        assertTrue(result is AppError.NetworkError)
        assertEquals(
            "The request timed out. Please try again.",
            result.userMessage
        )
        assertEquals(SuggestedAction.Retry, result.suggestedAction)
    }

    @Test
    fun `map 500 HttpException returns ServerError`() {
        val exception = HttpException(Response.error<Any>(500, okhttp3.ResponseBody.create(null, "")))
        val result = ErrorMapper.map(exception)

        assertTrue(result is AppError.ServerError)
        assertEquals(
            "A server error occurred (500). Please try again later.",
            result.userMessage
        )
        assertEquals(SuggestedAction.Retry, result.suggestedAction)
    }

    @Test
    fun `map 502 HttpException returns ServerError`() {
        val exception = HttpException(Response.error<Any>(502, okhttp3.ResponseBody.create(null, "")))
        val result = ErrorMapper.map(exception)

        assertTrue(result is AppError.ServerError)
        assertEquals(
            "A server error occurred (502). Please try again later.",
            result.userMessage
        )
        assertEquals(SuggestedAction.Retry, result.suggestedAction)
    }

    @Test
    fun `map 503 HttpException returns ServerError`() {
        val exception = HttpException(Response.error<Any>(503, okhttp3.ResponseBody.create(null, "")))
        val result = ErrorMapper.map(exception)

        assertTrue(result is AppError.ServerError)
        assertEquals(
            "A server error occurred (503). Please try again later.",
            result.userMessage
        )
        assertEquals(SuggestedAction.Retry, result.suggestedAction)
    }

    @Test
    fun `map unknown HttpException returns Unknown`() {
        val exception = HttpException(Response.error<Any>(418, okhttp3.ResponseBody.create(null, "")))
        val result = ErrorMapper.map(exception)

        assertTrue(result is AppError.Unknown)
        assertEquals(
            "An error occurred (418). Please try again.",
            result.userMessage
        )
        assertEquals(SuggestedAction.Retry, result.suggestedAction)
    }

    @Test
    fun `map generic exception returns Unknown`() {
        val exception = RuntimeException("Unexpected error")
        val result = ErrorMapper.map(exception)

        assertTrue(result is AppError.Unknown)
        assertTrue(result.userMessage.contains("Unexpected error"))
        assertEquals(SuggestedAction.Retry, result.suggestedAction)
    }
}
