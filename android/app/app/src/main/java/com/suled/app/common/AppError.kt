package com.suled.app.common

/**
 * Sealed class representing different types of errors in the application.
 * Provides type-safe error handling with specific error cases.
 * Extends [Throwable] to work with Kotlin's Result API.
 */
sealed class AppError(message: String? = null, cause: Throwable? = null) : Throwable(message, cause) {
    /**
     * Network connectivity error.
     * Occurs when device has no internet connection or network request fails.
     */
    data object NetworkError : AppError("Network connection error")
    
    /**
     * Server error (HTTP 5xx).
     * Indicates issues on the backend server.
     */
    data class ServerError(val code: Int, val serverMessage: String) : AppError("Server error: $code - $serverMessage")
    
    /**
     * Resource not found (HTTP 404).
     * Requested tournament, pair, or game does not exist.
     */
    data class NotFoundError(val resource: String) : AppError("$resource not found")
    
    /**
     * Bad request (HTTP 400).
     * Request was malformed or contains invalid parameters.
     */
    data class BadRequestError(val errorMessage: String) : AppError(errorMessage)
    
    /**
     * Authentication error (HTTP 401/403).
     * User is not authorized to access the resource.
     */
    data object UnauthorizedError : AppError("Unauthorized access")
    
    /**
     * Data parsing error.
     * Server response could not be parsed into expected format.
     */
    data class ParseError(val errorMessage: String) : AppError("Parse error: $errorMessage")
    
    /**
     * Database error.
     * Local database operation failed.
     */
    data class DatabaseError(val errorMessage: String) : AppError("Database error: $errorMessage")
    
    /**
     * Timeout error.
     * Request took too long to complete.
     */
    data object TimeoutError : AppError("Request timed out")
    
    /**
     * Unknown/unexpected error.
     * Catch-all for errors that don't fit other categories.
     */
    data class UnknownError(val errorMessage: String, val throwable: Throwable? = null) : AppError(errorMessage, throwable)
    
    /**
     * Converts this error to a user-friendly message.
     * @return Localized error message suitable for display
     */
    fun toUserMessage(): String {
        return when (this) {
            is NetworkError -> "No internet connection. Please check your network."
            is ServerError -> "Server error ($code). Please try again later."
            is NotFoundError -> "$resource not found."
            is BadRequestError -> errorMessage
            is UnauthorizedError -> "You are not authorized to access this resource."
            is ParseError -> "Error processing server response."
            is DatabaseError -> "Database error: $errorMessage"
            is TimeoutError -> "Request timed out. Please try again."
            is UnknownError -> errorMessage
        }
    }
}

/**
 * Extension function to convert generic exceptions to [AppError].
 */
fun Throwable.toAppError(): AppError {
    return when (this) {
        is java.net.UnknownHostException, 
        is java.net.SocketTimeoutException -> AppError.NetworkError
        is java.io.IOException -> AppError.NetworkError
        else -> AppError.UnknownError(
            errorMessage = this.message ?: "Unknown error occurred",
            throwable = this
        )
    }
}

/**
 * Converts HTTP response code to appropriate [AppError].
 * @param code HTTP status code
 * @param message Optional error message
 * @return Appropriate AppError for the status code
 */
fun httpErrorToAppError(code: Int, message: String = ""): AppError {
    return when (code) {
        in 400..499 -> when (code) {
            400 -> AppError.BadRequestError(message.ifEmpty { "Bad request (400)" })
            401, 403 -> AppError.UnauthorizedError
            404 -> AppError.NotFoundError("$message (404)".trim())
            408 -> AppError.TimeoutError
            else -> AppError.BadRequestError("Client error: $code${if (message.isNotEmpty()) " - $message" else ""}")
        }
        in 500..599 -> AppError.ServerError(code, message)
        else -> AppError.UnknownError("HTTP error: $code${if (message.isNotEmpty()) " - $message" else ""}")
    }
}
