package com.example.firebase

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed interface GoogleSignInResult {
    data class Success(val user: FirebaseUser) : GoogleSignInResult
    data class Cancelled(val message: String) : GoogleSignInResult
    data class Error(val message: String) : GoogleSignInResult
}

fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

object GoogleAuthHelper {

    private const val TAG = "Auth"

    suspend fun attemptAutoSignIn(activity: Activity): FirebaseUser? {
        if (Firebase.auth.currentUser != null) return Firebase.auth.currentUser
        val credentialManager = CredentialManager.create(activity)
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setAutoSelectEnabled(true)
                    .setServerClientId(activity.getString(R.string.default_web_client_id))
                    .build()
            )
            .build()

        return try {
            val response = credentialManager.getCredential(activity, request)
            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleToken = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCred = GoogleAuthProvider.getCredential(googleToken.idToken, null)
                val result = Firebase.auth.signInWithCredential(firebaseCred).await()
                result.user
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: NoCredentialException) {
            Log.d(TAG, "No previously authorized Google account for silent sign-in")
            null
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Silent Google auto sign-in skipped: ${e.message}")
            null
        } catch (e: Exception) {
            Log.w(TAG, "Silent Google auto sign-in failed: ${e.message}")
            null
        }
    }

    suspend fun onGoogleSignInClicked(activity: Activity): GoogleSignInResult {
        val credentialManager = CredentialManager.create(activity)
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetSignInWithGoogleOption.Builder(
                    activity.getString(R.string.default_web_client_id)
                ).build()
            )
            .build()

        return try {
            val response = credentialManager.getCredential(activity, request)
            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleToken = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCred = GoogleAuthProvider.getCredential(googleToken.idToken, null)
                val authResult = Firebase.auth.signInWithCredential(firebaseCred).await()
                val user = authResult.user
                if (user != null) {
                    GoogleSignInResult.Success(user)
                } else {
                    GoogleSignInResult.Error("Sign-in succeeded but no Firebase user was returned.")
                }
            } else {
                GoogleSignInResult.Error("Unexpected credential type received.")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            Log.w(
                TAG,
                "Google Sign-In cancelled (check SHA-1 registration in Firebase if user did not dismiss)",
                e
            )
            GoogleSignInResult.Cancelled("Sign-in was cancelled. Please select a Google account to continue.")
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Google Sign-In Credential Manager error", e)
            GoogleSignInResult.Error(e.message ?: "Google Sign-In failed. Please try again.")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase signInWithCredential error", e)
            GoogleSignInResult.Error(e.message ?: "Unable to sign in with Google.")
        }
    }

    suspend fun signOut(context: Context) {
        withContext(NonCancellable) {
            Firebase.auth.signOut()
            try {
                CredentialManager.create(context)
                    .clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "Failed to clear credential state on sign-out", e)
            }
        }
    }
}
