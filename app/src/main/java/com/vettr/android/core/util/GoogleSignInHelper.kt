package com.vettr.android.core.util

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.vettr.android.R
import timber.log.Timber

/**
 * Helper class for Google Sign-In using the Credential Manager API.
 * Manages the Google ID token retrieval flow for authentication.
 */
class GoogleSignInHelper(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)

    /**
     * Result of a Google Sign-In attempt.
     */
    sealed class GoogleSignInResult {
        data class Success(val idToken: String) : GoogleSignInResult()
        data class Error(val message: String) : GoogleSignInResult()
        data object Cancelled : GoogleSignInResult()
    }

    /**
     * Launch the Google Sign-In flow using Credential Manager.
     * Returns the Google ID token on success, or an error/cancellation result.
     */
    suspend fun signIn(): GoogleSignInResult {
        val webClientId = context.getString(R.string.google_web_client_id)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                request = request,
                context = context as android.app.Activity
            )
            handleSignInResult(result)
        } catch (e: GetCredentialCancellationException) {
            Timber.d("Google Sign-In cancelled by user")
            GoogleSignInResult.Cancelled
        } catch (e: NoCredentialException) {
            Timber.w(e, "No Google accounts available")
            GoogleSignInResult.Error("No Google accounts found on this device")
        } catch (e: GetCredentialException) {
            Timber.e(e, "Google Sign-In credential error")
            GoogleSignInResult.Error(e.message ?: "Google Sign-In failed")
        } catch (e: Exception) {
            Timber.e(e, "Google Sign-In unexpected error")
            GoogleSignInResult.Error(e.message ?: "An unexpected error occurred")
        }
    }

    /**
     * Extract the Google ID token from the credential response.
     */
    private fun handleSignInResult(response: GetCredentialResponse): GoogleSignInResult {
        val credential = response.credential

        return when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        if (idToken.isNotBlank()) {
                            GoogleSignInResult.Success(idToken)
                        } else {
                            GoogleSignInResult.Error("Empty ID token received")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to parse Google ID token")
                        GoogleSignInResult.Error("Failed to parse Google credentials")
                    }
                } else {
                    GoogleSignInResult.Error("Unexpected credential type: ${credential.type}")
                }
            }
            else -> {
                GoogleSignInResult.Error("Unexpected credential type")
            }
        }
    }
}
