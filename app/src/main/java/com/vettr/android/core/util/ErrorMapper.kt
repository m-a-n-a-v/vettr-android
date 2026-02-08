package com.vettr.android.core.util

import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Utility object to map exceptions to user-friendly AppError types.
 */
object ErrorMapper {

    /**
     * Maps a Throwable to an appropriate AppError type.
     *
     * @param throwable The exception to map
     * @return The corresponding AppError with user-friendly message and action
     */
    fun map(throwable: Throwable): AppError {
        return when (throwable) {
            // Network-related errors
            is UnknownHostException -> AppError.NetworkError(
                userMessage = "Unable to connect to the server. Please check your internet connection."
            )
            is SocketTimeoutException -> AppError.NetworkError(
                userMessage = "The request timed out. Please check your connection and try again."
            )
            is IOException -> AppError.NetworkError(
                userMessage = "A network error occurred. Please check your connection and try again."
            )

            // HTTP errors
            is HttpException -> mapHttpException(throwable)

            // Data parsing errors
            is JsonParseException -> AppError.InvalidData(
                userMessage = "Unable to process the server response. Please try again later."
            )

            // Default to unknown error
            else -> AppError.Unknown(
                userMessage = "An unexpected error occurred: ${throwable.message ?: "Unknown error"}"
            )
        }
    }

    /**
     * Maps HTTP exceptions based on status code.
     */
    private fun mapHttpException(exception: HttpException): AppError {
        return when (exception.code()) {
            400 -> AppError.InvalidData(
                userMessage = "Invalid request. Please check your input and try again."
            )
            401 -> AppError.Unauthorized(
                userMessage = "You are not authorized. Please sign in again."
            )
            403 -> AppError.Unauthorized(
                userMessage = "Access denied. You don't have permission to access this resource."
            )
            404 -> AppError.NotFound(
                userMessage = "The requested resource was not found."
            )
            408 -> AppError.NetworkError(
                userMessage = "The request timed out. Please try again."
            )
            500, 501, 502, 503, 504 -> AppError.ServerError(
                userMessage = "A server error occurred (${exception.code()}). Please try again later."
            )
            else -> AppError.Unknown(
                userMessage = "An error occurred (${exception.code()}). Please try again."
            )
        }
    }
}
