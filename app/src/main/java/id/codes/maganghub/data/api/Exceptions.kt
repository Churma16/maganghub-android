package id.codes.maganghub.data.api

import java.io.IOException

/**
 * Thrown when the MagangHub monev access token is expired or invalid (HTTP 401).
 */
class UnauthorizedSessionException(
    message: String = "Monev access token is expired or invalid (HTTP 401)"
) : IOException(message)

/**
 * Thrown when access is forbidden (HTTP 403).
 */
class ForbiddenException(
    message: String = "Access denied by MagangHub API (HTTP 403)"
) : IOException(message)

/**
 * Thrown when the endpoint is not found (HTTP 404).
 */
class NotFoundException(
    message: String = "Endpoint not found on MagangHub API (HTTP 404)"
) : IOException(message)

/**
 * Thrown when Kemnaker SSO authentication fails (bad credentials, CSRF mismatch, etc.).
 */
class KemnakerAuthException(
    message: String
) : IOException(message)

/**
 * Generic API exception for unexpected status codes.
 */
class MagangHubApiException(
    val statusCode: Int,
    message: String
) : IOException("MagangHub API error $statusCode: $message")
