package com.natrajtechnology.giggo.data.model

import com.google.firebase.Timestamp
import java.util.Date

data class Gig(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val deliveryTimeInDays: Int = 0,
    val requirements: String = "",
    val status: String = "DRAFT", // Changed to String to match Firebase storage
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val images: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val isActive: Boolean = true
) {
    // Helper property to get status as enum for UI logic
    val statusEnum: GigStatus
        get() = try {
            GigStatus.valueOf(status)
        } catch (e: Exception) {
            GigStatus.DRAFT
        }
}

enum class GigStatus {
    DRAFT,
    PUBLISHED,
    PAUSED,
    COMPLETED,
    CANCELLED
}

data class GigDraft(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val price: String = "", // Keep as string for draft to preserve user input
    val deliveryTime: String = "", // Keep as string for draft
    val requirements: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

// API Response Models
data class CreateGigRequest(
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val deliveryTimeInDays: Int,
    val requirements: String = "",
    val tags: List<String> = emptyList()
)

data class CreateGigResponse(
    val success: Boolean,
    val gigId: String? = null,
    val message: String = ""
)

data class SaveDraftRequest(
    val title: String,
    val description: String,
    val category: String,
    val price: String,
    val deliveryTime: String,
    val requirements: String = ""
)

data class SaveDraftResponse(
    val success: Boolean,
    val draftId: String? = null,
    val message: String = ""
)
