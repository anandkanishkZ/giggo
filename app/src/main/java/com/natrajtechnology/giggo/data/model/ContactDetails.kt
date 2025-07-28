package com.natrajtechnology.giggo.data.model

data class ContactDetails(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val profileImageUrl: String = "",
    val location: String = "",
    val joinDate: String = "",
    val completedGigs: Int = 0,
    val rating: Float = 0.0f,
    val isVerified: Boolean = false,
    val skills: List<String> = emptyList(),
    val bio: String = ""
)
