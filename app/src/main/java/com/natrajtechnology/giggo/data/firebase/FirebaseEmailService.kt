package com.natrajtechnology.giggo.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.natrajtechnology.giggo.data.model.ContactSellerRequest
import com.natrajtechnology.giggo.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseEmailService @Inject constructor() {
    
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val usersCollection = firestore.collection("users")
    private val emailQueueCollection = firestore.collection("emailQueue")
    
    /**
     * Sends an email notification to the seller when a buyer contacts them
     * This creates an email document in Firestore that will be processed by a Cloud Function
     */
    suspend fun sendContactSellerEmail(request: ContactSellerRequest): Boolean {
        return try {
            println("Debug: FirebaseEmailService - Starting email notification process")
            
            // Validate input
            if (request.sellerUserId.isBlank()) {
                println("Debug: FirebaseEmailService - Seller user ID is blank")
                return false
            }
            if (request.buyerUserId.isBlank()) {
                println("Debug: FirebaseEmailService - Buyer user ID is blank")
                return false
            }
            
            // Get seller's email from user document
            val sellerEmail = getSellerEmail(request.sellerUserId)
            if (sellerEmail.isBlank()) {
                println("Debug: FirebaseEmailService - Could not retrieve seller email for user: ${request.sellerUserId}")
                return false
            }
            
            // Get buyer's information
            val buyerInfo = getBuyerInfo(request.buyerUserId)
            val buyerName = buyerInfo?.displayName ?: request.buyerName
            val buyerEmail = buyerInfo?.email ?: "No email provided"
            
            println("Debug: FirebaseEmailService - Seller email: $sellerEmail")
            println("Debug: FirebaseEmailService - Buyer: $buyerName ($buyerEmail)")
            
            // Create email document for Cloud Function to process
            val emailData = mapOf(
                "to" to sellerEmail,
                "subject" to "New Contact Request for Your Gig: ${request.gigTitle}",
                "template" to "contact_seller",
                "templateData" to mapOf(
                    "sellerName" to "Seller", // Can be enhanced to get actual seller name
                    "buyerName" to buyerName,
                    "buyerEmail" to buyerEmail,
                    "gigTitle" to request.gigTitle,
                    "gigId" to request.gigId,
                    "message" to request.message,
                    "contactDate" to System.currentTimeMillis(),
                    "appName" to "GigGO"
                ),
                "priority" to "high",
                "createdAt" to System.currentTimeMillis(),
                "processed" to false,
                "status" to "pending"
            )
            
            // Add to email queue for processing
            val emailDocumentRef = emailQueueCollection.document()
            emailDocumentRef.set(emailData).await()
            
            println("Debug: FirebaseEmailService - Email queued successfully with ID: ${emailDocumentRef.id}")
            println("Debug: FirebaseEmailService - Email data: $emailData")
            true
            
        } catch (e: Exception) {
            println("Debug: FirebaseEmailService - Error sending email notification: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Gets the seller's email address from their user document
     */
    private suspend fun getSellerEmail(sellerUserId: String): String {
        return try {
            println("Debug: FirebaseEmailService - Fetching seller email for user: $sellerUserId")
            val userDocument = usersCollection.document(sellerUserId).get().await()
            
            if (!userDocument.exists()) {
                println("Debug: FirebaseEmailService - User document does not exist for: $sellerUserId")
                return ""
            }
            
            val user = userDocument.toObject(User::class.java)
            val email = user?.email ?: ""
            
            if (email.isBlank()) {
                println("Debug: FirebaseEmailService - User $sellerUserId has no email address")
            } else {
                println("Debug: FirebaseEmailService - Found email for user $sellerUserId: $email")
            }
            
            email
        } catch (e: Exception) {
            println("Debug: FirebaseEmailService - Error getting seller email: ${e.message}")
            e.printStackTrace()
            ""
        }
    }
    
    /**
     * Gets buyer information from their user document
     */
    private suspend fun getBuyerInfo(buyerUserId: String): User? {
        return try {
            println("Debug: FirebaseEmailService - Fetching buyer info for user: $buyerUserId")
            val userDocument = usersCollection.document(buyerUserId).get().await()
            
            if (!userDocument.exists()) {
                println("Debug: FirebaseEmailService - Buyer document does not exist for: $buyerUserId")
                return null
            }
            
            val user = userDocument.toObject(User::class.java)
            println("Debug: FirebaseEmailService - Found buyer: ${user?.displayName} (${user?.email})")
            user
        } catch (e: Exception) {
            println("Debug: FirebaseEmailService - Error getting buyer info: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Sends a welcome email to new users
     */
    suspend fun sendWelcomeEmail(userEmail: String, userName: String): Boolean {
        return try {
            val emailData = mapOf(
                "to" to userEmail,
                "subject" to "Welcome to GigGO!",
                "template" to "welcome",
                "templateData" to mapOf(
                    "userName" to userName,
                    "appName" to "GigGO",
                    "supportEmail" to "support@giggo.com"
                ),
                "priority" to "normal",
                "createdAt" to System.currentTimeMillis(),
                "processed" to false,
                "status" to "pending"
            )
            
            emailQueueCollection.document().set(emailData).await()
            println("Debug: FirebaseEmailService - Welcome email queued for $userEmail")
            true
        } catch (e: Exception) {
            println("Debug: FirebaseEmailService - Error sending welcome email: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Checks the status of recent email queue items for debugging
     */
    suspend fun checkEmailQueueStatus(): List<Map<String, Any>> {
        return try {
            val snapshot = emailQueueCollection
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()
            
            val emailStatuses = mutableListOf<Map<String, Any>>()
            snapshot.documents.forEach { doc ->
                val data = doc.data ?: return@forEach
                emailStatuses.add(
                    mapOf(
                        "id" to doc.id,
                        "to" to (data["to"] ?: "unknown"),
                        "template" to (data["template"] ?: "unknown"),
                        "status" to (data["status"] ?: "pending"),
                        "processed" to (data["processed"] ?: false),
                        "createdAt" to (data["createdAt"] ?: 0),
                        "error" to (data["error"] ?: "none")
                    )
                )
            }
            
            println("Debug: FirebaseEmailService - Recent email queue status:")
            emailStatuses.forEach { status ->
                println("  ${status["id"]}: ${status["to"]} - ${status["status"]} (processed: ${status["processed"]})")
            }
            
            emailStatuses
        } catch (e: Exception) {
            println("Debug: FirebaseEmailService - Error checking email queue: ${e.message}")
            emptyList()
        }
    }
}
