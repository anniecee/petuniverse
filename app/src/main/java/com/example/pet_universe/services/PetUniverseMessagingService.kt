package com.example.pet_universe.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PetUniverseMessagingService : FirebaseMessagingService() {
    private lateinit var notificationService: NotificationService

    override fun onCreate() {
        super.onCreate()
        notificationService = NotificationService(this)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        println("FCM Message received: ${remoteMessage.data}")

        // Check if current user is the sender
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val senderId = remoteMessage.data["senderId"]
        
        if (currentUserId == null) {
            println("FCM: No current user")
            return
        }
        
        if (currentUserId == senderId) {
            println("FCM: Message from self, ignoring")
            return
        }

        val senderName = remoteMessage.data["senderName"] 
            ?: remoteMessage.notification?.title 
            ?: "New Message"
        val message = remoteMessage.notification?.body 
            ?: remoteMessage.data["message"] 
            ?: run {
                println("FCM: No message content")
                return
            }

        println("FCM: Showing notification for $senderName: $message")
        notificationService.showMessageNotification(
            senderId ?: return,
            senderName,
            message
        )
    }

    override fun onNewToken(token: String) {
        println("FCM: New token received: $token")
        // Store token in Firestore if user is logged in
        FirebaseAuth.getInstance().currentUser?.let { user ->
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .update("fcmToken", token)
        }
    }
}