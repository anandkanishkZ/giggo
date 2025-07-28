package com.natrajtechnology.giggo.data.model

import com.google.firebase.Timestamp

data class Notification(
    val id: String = "",
    val recipientUserId: String = "", // User who receives the notification
    val senderUserId: String = "",    // User who sent the notification (contacted seller)
    val senderName: String = "",      // Name of the sender for display
    val gigId: String = "",           // Related gig ID
    val gigTitle: String = "",        // Gig title for display
    val type: NotificationCategory = NotificationCategory.CONTACT_REQUEST,
    val title: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)

enum class NotificationCategory {
    CONTACT_REQUEST,  // When someone contacts about a gig
    MESSAGE,          // General messages
    JOB_ALERT,        // Job alerts
    SYSTEM            // System notifications
}

data class ContactSellerRequest(
    val gigId: String,
    val gigTitle: String,
    val sellerUserId: String,
    val buyerUserId: String,
    val buyerName: String,
    val message: String = ""
)

data class ContactSellerResponse(
    val success: Boolean,
    val notificationId: String? = null,
    val message: String = ""
)
