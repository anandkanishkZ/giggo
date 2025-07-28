package com.natrajtechnology.giggo.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val profileImageUrl: String = "",
    val phoneNumber: String = "",
    val isEmailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", "", "", false, 0L)
    
    val displayName: String
        get() = "$firstName $lastName".trim()
}
