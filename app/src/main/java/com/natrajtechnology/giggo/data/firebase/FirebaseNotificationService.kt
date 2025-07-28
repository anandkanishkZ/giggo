package com.natrajtechnology.giggo.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.natrajtechnology.giggo.data.model.ContactSellerRequest
import com.natrajtechnology.giggo.data.model.ContactSellerResponse
import com.natrajtechnology.giggo.data.model.Notification
import com.natrajtechnology.giggo.data.model.NotificationCategory
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseNotificationService @Inject constructor(
    private val emailService: FirebaseEmailService
) {
    
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val notificationsCollection = firestore.collection("notifications")
    
    /**
     * Sends a contact seller notification
     */
    suspend fun sendContactSellerNotification(request: ContactSellerRequest): ContactSellerResponse {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                println("Debug: FirebaseNotificationService - User not authenticated")
                return ContactSellerResponse(
                    success = false,
                    message = "User not authenticated"
                )
            }

            println("Debug: FirebaseNotificationService - Sending contact seller notification")
            println("Debug: Current User ID: ${currentUser.uid}")
            println("Debug: Seller ID: ${request.sellerUserId}, Buyer: ${request.buyerName}")
            println("Debug: Gig: ${request.gigTitle}")
            println("Debug: Buyer User ID: ${request.buyerUserId}")
            println("Debug: Message: ${request.message}")

            // Don't send notification to yourself
            if (currentUser.uid == request.sellerUserId) {
                println("Debug: FirebaseNotificationService - Cannot contact yourself")
                return ContactSellerResponse(
                    success = false,
                    message = "You cannot contact yourself about your own gig"
                )
            }

            val notificationId = notificationsCollection.document().id
            val currentTimestamp = com.google.firebase.Timestamp.now()
            val notification = Notification(
                id = notificationId,
                recipientUserId = request.sellerUserId,
                senderUserId = request.buyerUserId,
                senderName = request.buyerName,
                gigId = request.gigId,
                gigTitle = request.gigTitle,
                type = NotificationCategory.CONTACT_REQUEST,
                title = "New Contact Request",
                message = "${request.buyerName} is interested in your gig: \"${request.gigTitle}\"" +
                         if (request.message.isNotBlank()) ". Message: ${request.message}" else "",
                isRead = false,
                createdAt = currentTimestamp
            )

            println("Debug: FirebaseNotificationService - Creating notification with ID: $notificationId")
            println("Debug: FirebaseNotificationService - Notification data: $notification")

            try {
                val result = notificationsCollection.document(notificationId).set(notification).await()
                println("Debug: FirebaseNotificationService - Firestore set result: $result")
                
                // Send email notification to seller
                println("Debug: FirebaseNotificationService - Sending email notification to seller")
                val emailSent = emailService.sendContactSellerEmail(request)
                if (emailSent) {
                    println("Debug: FirebaseNotificationService - Email notification queued successfully")
                } else {
                    println("Debug: FirebaseNotificationService - Failed to queue email notification")
                    // Still consider the notification successful even if email fails
                }
                
            } catch (e: Exception) {
                println("Debug: FirebaseNotificationService - Firestore set error: ${e.message}")
                e.printStackTrace()
                return ContactSellerResponse(
                    success = false,
                    message = "Failed to write notification: ${e.message}"
                )
            }

            println("Debug: FirebaseNotificationService - Notification sent successfully to Firestore")

            ContactSellerResponse(
                success = true,
                notificationId = notificationId,
                message = "Contact request sent successfully!"
            )

        } catch (e: Exception) {
            println("Debug: FirebaseNotificationService - Error sending notification: ${e.message}")
            e.printStackTrace()
            ContactSellerResponse(
                success = false,
                message = "Failed to send contact request: ${e.message}"
            )
        }
    }
    
    /**
     * Gets email queue status for debugging
     */
    suspend fun getEmailQueueStatus(): List<Map<String, Any>> {
        return emailService.checkEmailQueueStatus()
    }
    
    /**
     * Gets all notifications for the current user
     */
    suspend fun getUserNotifications(): List<Notification> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                println("Debug: FirebaseNotificationService - No current user for getting notifications")
                return emptyList()
            }

            println("Debug: FirebaseNotificationService - Getting notifications for user: ${currentUser.uid}")

            // Remove orderBy to avoid composite index requirement for now
            // Sorting will be done in memory temporarily
            val query = notificationsCollection
                .whereEqualTo("recipientUserId", currentUser.uid)
            println("Debug: FirebaseNotificationService - Firestore query: recipientUserId=${currentUser.uid}")
            val snapshot = query.get().await()
            println("Debug: FirebaseNotificationService - Query completed. Found ${snapshot.documents.size} documents")
            snapshot.documents.forEach { doc ->
                println("Debug: Firestore document ID: ${doc.id}, data: ${doc.data}")
            }
            val notifications = snapshot.documents.mapNotNull { doc ->
                try {
                    val notification = doc.toObject(Notification::class.java)
                    println("Debug: FirebaseNotificationService - Parsed notification: ${notification?.id} - ${notification?.title}")
                    notification
                } catch (e: Exception) {
                    println("Debug: Error parsing notification ${doc.id}: ${e.message}")
                    e.printStackTrace()
                    null
                }
            }.sortedByDescending { it.createdAt.seconds } // Sort by timestamp in memory

            println("Debug: FirebaseNotificationService - Successfully parsed ${notifications.size} notifications")

            notifications
        } catch (e: Exception) {
            println("Debug: FirebaseNotificationService - Error loading notifications: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Marks a notification as read
     */
    suspend fun markNotificationAsRead(notificationId: String): Boolean {
        return try {
            notificationsCollection.document(notificationId)
                .update("isRead", true)
                .await()
            
            println("Debug: FirebaseNotificationService - Marked notification $notificationId as read")
            true
        } catch (e: Exception) {
            println("Debug: FirebaseNotificationService - Error marking notification as read: ${e.message}")
            false
        }
    }
    
    /**
     * Marks all notifications as read for current user
     */
    suspend fun markAllNotificationsAsRead(): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            
            val unreadNotifications = notificationsCollection
                .whereEqualTo("recipientUserId", currentUser.uid)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            val batch = firestore.batch()
            unreadNotifications.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            
            batch.commit().await()
            
            println("Debug: FirebaseNotificationService - Marked ${unreadNotifications.size()} notifications as read")
            true
        } catch (e: Exception) {
            println("Debug: FirebaseNotificationService - Error marking all notifications as read: ${e.message}")
            false
        }
    }
    
    /**
     * Deletes a notification
     */
    suspend fun deleteNotification(notificationId: String): Boolean {
        return try {
            notificationsCollection.document(notificationId).delete().await()
            
            println("Debug: FirebaseNotificationService - Deleted notification $notificationId")
            true
        } catch (e: Exception) {
            println("Debug: FirebaseNotificationService - Error deleting notification: ${e.message}")
            false
        }
    }
    
    /**
     * Gets detailed contact information for a user
     */
    suspend fun getUserContactDetails(userId: String): com.natrajtechnology.giggo.data.model.ContactDetails? {
        return try {
            println("Debug: FirebaseNotificationService - Fetching contact details for user: $userId")
            
            val userDocument = firestore.collection("users").document(userId).get().await()
            
            if (!userDocument.exists()) {
                println("Debug: FirebaseNotificationService - User document does not exist for: $userId")
                return null
            }
            
            val userData = userDocument.data
            if (userData == null) {
                println("Debug: FirebaseNotificationService - User data is null for: $userId")
                return null
            }
            
            println("Debug: FirebaseNotificationService - Raw user data: $userData")
            
            // Build display name from firstName and lastName if displayName doesn't exist
            val firstName = userData["firstName"] as? String ?: ""
            val lastName = userData["lastName"] as? String ?: ""
            val displayName = userData["displayName"] as? String 
                ?: userData["name"] as? String 
                ?: if (firstName.isNotBlank() || lastName.isNotBlank()) {
                    "$firstName $lastName".trim()
                } else {
                    "Unknown User"
                }
            
            // Format join date properly
            val joinDate = when (val createdAt = userData["createdAt"]) {
                is Long -> {
                    val dateFormat = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault())
                    dateFormat.format(java.util.Date(createdAt))
                }
                is String -> createdAt
                else -> "Recently joined"
            }
            
            // Get additional stats (this could be enhanced with actual data from other collections)
            val contactDetails = com.natrajtechnology.giggo.data.model.ContactDetails(
                userId = userId,
                name = displayName,
                email = userData["email"] as? String ?: "",
                phone = userData["phone"] as? String 
                    ?: userData["phoneNumber"] as? String 
                    ?: userData["phone_number"] as? String ?: "",
                profileImageUrl = userData["profileImageUrl"] as? String 
                    ?: userData["photoURL"] as? String 
                    ?: userData["profile_image_url"] as? String ?: "",
                location = userData["location"] as? String 
                    ?: userData["address"] as? String 
                    ?: userData["city"] as? String ?: "",
                joinDate = joinDate,
                completedGigs = when (val completed = userData["completedGigs"]) {
                    is Long -> completed.toInt()
                    is Int -> completed
                    is String -> completed.toIntOrNull() ?: 0
                    else -> 0
                },
                rating = when (val rating = userData["rating"]) {
                    is Double -> rating.toFloat()
                    is Float -> rating
                    is Long -> rating.toFloat()
                    is Int -> rating.toFloat()
                    is String -> rating.toFloatOrNull() ?: 0.0f
                    else -> 0.0f
                },
                isVerified = userData["isVerified"] as? Boolean 
                    ?: userData["emailVerified"] as? Boolean 
                    ?: userData["is_verified"] as? Boolean ?: false,
                skills = (userData["skills"] as? List<*>)?.filterIsInstance<String>() 
                    ?: listOf("Android Developer"), // Default skill for demo
                bio = userData["bio"] as? String 
                    ?: userData["description"] as? String 
                    ?: userData["about"] as? String 
                    ?: "Experienced professional ready to help with your project."
            )
            
            println("Debug: FirebaseNotificationService - Found contact details: ${contactDetails.name} (${contactDetails.email})")
            println("Debug: FirebaseNotificationService - Phone: ${contactDetails.phone}, Location: ${contactDetails.location}")
            println("Debug: FirebaseNotificationService - Skills: ${contactDetails.skills}, Bio: ${contactDetails.bio}")
            
            contactDetails
            
        } catch (e: Exception) {
            println("Debug: FirebaseNotificationService - Error getting contact details: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
