package com.example.pet_universe

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        // Enable debug provider for App Check
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        // Listen for auth state changes
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                updateFCMToken()
            }
        }

        // Request FCM token immediately after app initialization
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@addOnCompleteListener
                }
                val token = task.result
                FirebaseAuth.getInstance().currentUser?.let { user ->
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.uid)
                        .update("fcmToken", token)
                        .addOnFailureListener { e ->
                            println("Error updating FCM token: $e")
                        }
                }
            }
    }

    private fun updateFCMToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    FirebaseAuth.getInstance().currentUser?.let { user ->
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(user.uid)
                            .update("fcmToken", token)
                    }
                }
            }
    }
}
