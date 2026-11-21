package com.example.firebase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType {
    CREATE, UPDATE, DELETE, LIST, GET, WRITE
}

fun handleFirestoreError(
    error: Throwable,
    operationType: OperationType,
    path: String?
): Nothing {
    val user = Firebase.auth.currentUser
    val payload = JSONObject().apply {
        put("error", error.message ?: error.toString())
        put("operationType", operationType.name.lowercase())
        put("path", path ?: JSONObject.NULL)
        put("authInfo", JSONObject().apply {
            put("userId", user?.uid ?: JSONObject.NULL)
            put("email", user?.email ?: JSONObject.NULL)
            put("emailVerified", user?.isEmailVerified ?: false)
            put("isAnonymous", user?.isAnonymous ?: false)
            put("providerInfo", JSONArray().apply {
                user?.providerData?.forEach { p ->
                    put(JSONObject().apply {
                        put("providerId", p.providerId)
                        put("displayName", p.displayName ?: JSONObject.NULL)
                        put("email", p.email ?: JSONObject.NULL)
                    })
                }
            })
        })
    }.toString()

    Log.e("FirestoreError", payload, error)
    throw RuntimeException(payload, error)
}
