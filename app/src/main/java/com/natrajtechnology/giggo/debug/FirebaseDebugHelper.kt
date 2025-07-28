package com.natrajtechnology.giggo.debug

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Debug helper class to troubleshoot Firebase issues
 */
object FirebaseDebugHelper {
    
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    /**
     * Checks current authentication state
     */
    fun checkAuthState(): String {
        val currentUser = auth.currentUser
        return if (currentUser != null) {
            "Authenticated: ${currentUser.uid} (${currentUser.email})"
        } else {
            "Not authenticated"
        }
    }
    
    /**
     * Counts total notifications in the collection
     */
    suspend fun getTotalNotificationCount(): String {
        return try {
            val snapshot = firestore.collection("notifications").get().await()
            "Total notifications in collection: ${snapshot.documents.size}"
        } catch (e: Exception) {
            "Error counting notifications: ${e.message}"
        }
    }
    
    /**
     * Counts notifications for current user
     */
    suspend fun getUserNotificationCount(): String {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return "No authenticated user"
            }
            
            val snapshot = firestore.collection("notifications")
                .whereEqualTo("recipientUserId", currentUser.uid)
                .get()
                .await()
            
            "Notifications for user ${currentUser.uid}: ${snapshot.documents.size}"
        } catch (e: Exception) {
            "Error counting user notifications: ${e.message}"
        }
    }
    
    /**
     * Lists all notification documents with basic info
     */
    suspend fun listAllNotifications(): String {
        return try {
            val snapshot = firestore.collection("notifications").get().await()
            val notifications = mutableListOf<String>()
            
            snapshot.documents.forEach { doc ->
                val data = doc.data
                val recipientId = data?.get("recipientUserId") as? String ?: "unknown"
                val title = data?.get("title") as? String ?: "unknown"
                val isRead = data?.get("isRead") as? Boolean ?: false
                notifications.add("ID: ${doc.id}, Recipient: $recipientId, Title: $title, Read: $isRead")
            }
            
            if (notifications.isEmpty()) {
                "No notifications found in database"
            } else {
                "All notifications:\n${notifications.joinToString("\n")}"
            }
        } catch (e: Exception) {
            "Error listing notifications: ${e.message}"
        }
    }
    
    /**
     * Tests Firebase connection
     */
    suspend fun testFirebaseConnection(): String {
        return try {
            // Try to read a simple document
            val testRef = firestore.collection("test").document("connection")
            testRef.set(mapOf("timestamp" to System.currentTimeMillis())).await()
            testRef.delete().await()
            "Firebase connection: WORKING"
        } catch (e: Exception) {
            "Firebase connection error: ${e.message}"
        }
    }
    
    /**
     * Creates a test notification for debugging
     */
    suspend fun createTestNotification(): String {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return "Cannot create test notification: No authenticated user"
            }
            
            val testNotification = mapOf(
                "id" to "test_${System.currentTimeMillis()}",
                "recipientUserId" to currentUser.uid,
                "senderUserId" to "test_sender",
                "senderName" to "Test Sender",
                "gigId" to "test_gig",
                "gigTitle" to "Debug Test Gig",
                "type" to "CONTACT_REQUEST",
                "title" to "Test Notification",
                "message" to "This is a test notification created for debugging",
                "isRead" to false,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            
            val docRef = firestore.collection("notifications").document()
            docRef.set(testNotification).await()
            
            "Test notification created successfully with ID: ${docRef.id}"
        } catch (e: Exception) {
            "Error creating test notification: ${e.message}"
        }
    }
}
