package com.natrajtechnology.giggo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.natrajtechnology.giggo.data.firebase.FirebaseNotificationService
import com.natrajtechnology.giggo.data.model.ContactSellerRequest
import com.natrajtechnology.giggo.data.model.ContactSellerResponse
import com.natrajtechnology.giggo.data.model.Notification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationService: FirebaseNotificationService
) : ViewModel() {
    
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _contactSellerResponse = MutableStateFlow<ContactSellerResponse?>(null)
    val contactSellerResponse: StateFlow<ContactSellerResponse?> = _contactSellerResponse.asStateFlow()
    
    private val _contactDetails = MutableStateFlow<com.natrajtechnology.giggo.data.model.ContactDetails?>(null)
    val contactDetails: StateFlow<com.natrajtechnology.giggo.data.model.ContactDetails?> = _contactDetails.asStateFlow()
    
    private val _showContactDetailsDialog = MutableStateFlow(false)
    val showContactDetailsDialog: StateFlow<Boolean> = _showContactDetailsDialog.asStateFlow()
    
    init {
        loadNotifications()
    }
    
    /**
     * Loads all notifications for the current user
     */
    fun loadNotifications() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                println("Debug: NotificationViewModel - Starting loadNotifications")
                
                val notificationList = notificationService.getUserNotifications()
                _notifications.value = notificationList
                
                println("Debug: NotificationViewModel - Loaded ${notificationList.size} notifications")
                notificationList.forEach { notification ->
                    println("Debug: NotificationViewModel - Notification: ${notification.id} - ${notification.title} - Read: ${notification.isRead}")
                }
                
            } catch (e: Exception) {
                println("Debug: NotificationViewModel - Error loading notifications: ${e.message}")
                e.printStackTrace()
                _errorMessage.value = "Failed to load notifications: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Sends a contact seller request notification
     */
    fun sendContactSellerRequest(request: ContactSellerRequest) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                println("Debug: NotificationViewModel - Sending contact seller request")
                println("Debug: NotificationViewModel - Request: $request")
                
                val response = notificationService.sendContactSellerNotification(request)
                _contactSellerResponse.value = response
                
                if (response.success) {
                    println("Debug: NotificationViewModel - Contact seller request sent successfully")
                    println("Debug: NotificationViewModel - Notification ID: ${response.notificationId}")
                    // Refresh notifications to show any new notifications
                    loadNotifications()
                } else {
                    println("Debug: NotificationViewModel - Failed to send contact seller request: ${response.message}")
                    _errorMessage.value = response.message
                }
                
            } catch (e: Exception) {
                println("Debug: NotificationViewModel - Error sending contact seller request: ${e.message}")
                e.printStackTrace()
                _errorMessage.value = "Failed to send contact request: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Marks a notification as read
     */
    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                val success = notificationService.markNotificationAsRead(notificationId)
                if (success) {
                    // Update local state
                    val updatedNotifications = _notifications.value.map { notification ->
                        if (notification.id == notificationId) {
                            notification.copy(isRead = true)
                        } else {
                            notification
                        }
                    }
                    _notifications.value = updatedNotifications
                    
                    println("Debug: NotificationViewModel - Marked notification $notificationId as read")
                }
            } catch (e: Exception) {
                println("Debug: NotificationViewModel - Error marking notification as read: ${e.message}")
                _errorMessage.value = "Failed to mark notification as read"
            }
        }
    }
    
    /**
     * Marks all notifications as read
     */
    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            try {
                val success = notificationService.markAllNotificationsAsRead()
                if (success) {
                    // Update local state
                    val updatedNotifications = _notifications.value.map { notification ->
                        notification.copy(isRead = true)
                    }
                    _notifications.value = updatedNotifications
                    
                    println("Debug: NotificationViewModel - Marked all notifications as read")
                }
            } catch (e: Exception) {
                println("Debug: NotificationViewModel - Error marking all notifications as read: ${e.message}")
                _errorMessage.value = "Failed to mark all notifications as read"
            }
        }
    }
    
    /**
     * Deletes a notification
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                val success = notificationService.deleteNotification(notificationId)
                if (success) {
                    // Remove from local state
                    val updatedNotifications = _notifications.value.filter { it.id != notificationId }
                    _notifications.value = updatedNotifications
                    
                    println("Debug: NotificationViewModel - Deleted notification $notificationId")
                }
            } catch (e: Exception) {
                println("Debug: NotificationViewModel - Error deleting notification: ${e.message}")
                _errorMessage.value = "Failed to delete notification"
            }
        }
    }
    
    /**
     * Gets the count of unread notifications
     */
    fun getUnreadNotificationCount(): Int {
        return _notifications.value.count { !it.isRead }
    }
    
    /**
     * Clears the contact seller response
     */
    fun clearContactSellerResponse() {
        _contactSellerResponse.value = null
    }
    
    /**
     * Clears the error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    /**
     * Refreshes notifications by reloading from Firebase
     */
    fun refreshNotifications() {
        loadNotifications()
    }
    
    /**
     * Creates a test notification for debugging purposes
     */
    fun createTestNotification() {
        viewModelScope.launch {
            try {
                println("Debug: NotificationViewModel - Creating test notification")
                
                // First, create a test user in Firestore to ensure contact details exist
                val testUserId = "test_buyer_contact_demo"
                val testUserData = mapOf(
                    "displayName" to "John Test Contact",
                    "email" to "john.test@example.com",
                    "phone" to "+1234567890",
                    "profileImageUrl" to "",
                    "location" to "New York, USA",
                    "createdAt" to System.currentTimeMillis(),
                    "completedGigs" to 5,
                    "rating" to 4.5,
                    "isVerified" to true,
                    "skills" to listOf("Flutter", "Firebase", "UI/UX"),
                    "bio" to "Experienced mobile developer with 5+ years in Flutter development."
                )
                
                // Save test user to Firestore
                try {
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(testUserId)
                        .set(testUserData)
                        .await()
                    println("Debug: NotificationViewModel - Test user created successfully")
                } catch (e: Exception) {
                    println("Debug: NotificationViewModel - Error creating test user: ${e.message}")
                }
                
                // Create a test contact seller request with the test user
                val testRequest = ContactSellerRequest(
                    gigId = "test_gig_contact_demo",
                    gigTitle = "Test Flutter Development Gig for Contact Demo",
                    sellerUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "current_seller",
                    buyerUserId = testUserId,
                    buyerName = "John Test Contact",
                    message = "Hi, I'm interested in your gig. This is a test message to demonstrate the contact details popup."
                )
                
                val response = notificationService.sendContactSellerNotification(testRequest)
                println("Debug: NotificationViewModel - Test notification created: ${response.success}")
                
                if (response.success) {
                    loadNotifications() // Refresh to show the new notification
                    println("Debug: NotificationViewModel - Test notification with contact data ready!")
                } else {
                    println("Debug: NotificationViewModel - Failed to create test notification: ${response.message}")
                }
                
            } catch (e: Exception) {
                println("Debug: NotificationViewModel - Error creating test notification: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Comprehensive debug function to diagnose notification issues
     */
    fun debugNotificationSystem() {
        viewModelScope.launch {
            try {
                val debugHelper = com.natrajtechnology.giggo.debug.FirebaseDebugHelper
                
                println("=== NOTIFICATION DEBUG REPORT ===")
                println("1. Auth State: ${debugHelper.checkAuthState()}")
                println("2. Firebase Connection: ${debugHelper.testFirebaseConnection()}")
                println("3. Total Notifications: ${debugHelper.getTotalNotificationCount()}")
                println("4. User Notifications: ${debugHelper.getUserNotificationCount()}")
                println("5. All Notifications:")
                println(debugHelper.listAllNotifications())
                println("=== END DEBUG REPORT ===")
                
            } catch (e: Exception) {
                println("Debug: NotificationViewModel - Error running debug: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Checks email queue status for debugging
     */
    fun checkEmailQueueStatus() {
        viewModelScope.launch {
            try {
                println("Debug: NotificationViewModel - Checking email queue status")
                
                // Access the email service through the notification service
                // We'll need to add this method to notification service
                
            } catch (e: Exception) {
                println("Debug: NotificationViewModel - Error checking email queue: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Loads contact details for a specific user and shows the dialog
     */
    fun loadContactDetails(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                println("Debug: NotificationViewModel - Loading contact details for user: $userId")
                
                // Validate userId
                if (userId.isBlank()) {
                    println("Debug: NotificationViewModel - Invalid user ID (blank)")
                    _errorMessage.value = "Invalid user ID"
                    return@launch
                }
                
                val details = notificationService.getUserContactDetails(userId)
                if (details != null) {
                    println("Debug: NotificationViewModel - Contact details loaded successfully for: ${details.name}")
                    _contactDetails.value = details
                    _showContactDetailsDialog.value = true
                } else {
                    println("Debug: NotificationViewModel - Failed to load contact details for user: $userId")
                    _errorMessage.value = "Could not load contact details. The user profile may not be complete."
                    
                    // Try to create a minimal contact details from the notification data
                    val notification = _notifications.value.find { it.senderUserId == userId }
                    if (notification != null) {
                        println("Debug: NotificationViewModel - Creating minimal contact details from notification data")
                        val minimalDetails = com.natrajtechnology.giggo.data.model.ContactDetails(
                            userId = userId,
                            name = notification.senderName,
                            email = "Contact information not available",
                            phone = "",
                            profileImageUrl = "",
                            location = "",
                            joinDate = "Recently joined",
                            completedGigs = 0,
                            rating = 0.0f,
                            isVerified = false,
                            skills = listOf("GigGO User"),
                            bio = "This user contacted you about: ${notification.gigTitle}"
                        )
                        _contactDetails.value = minimalDetails
                        _showContactDetailsDialog.value = true
                    }
                }
                
            } catch (e: Exception) {
                println("Debug: NotificationViewModel - Error loading contact details: ${e.message}")
                e.printStackTrace()
                _errorMessage.value = "Failed to load contact details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Hides the contact details dialog
     */
    fun hideContactDetailsDialog() {
        _showContactDetailsDialog.value = false
        _contactDetails.value = null
    }
}
