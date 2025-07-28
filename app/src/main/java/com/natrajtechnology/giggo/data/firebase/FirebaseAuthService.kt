package com.natrajtechnology.giggo.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.natrajtechnology.giggo.data.model.AuthResult
import com.natrajtechnology.giggo.data.model.LoginRequest
import com.natrajtechnology.giggo.data.model.SignUpRequest
import com.natrajtechnology.giggo.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    suspend fun signUp(request: SignUpRequest): Flow<AuthResult> = flow {
        try {
            emit(AuthResult.Loading)
            
            // Validate input
            when {
                request.firstName.isBlank() -> {
                    emit(AuthResult.Error("First name is required"))
                    return@flow
                }
                request.lastName.isBlank() -> {
                    emit(AuthResult.Error("Last name is required"))
                    return@flow
                }
                !request.email.isValidEmail() -> {
                    emit(AuthResult.Error("Invalid email format"))
                    return@flow
                }
                request.password.length < 6 -> {
                    emit(AuthResult.Error("Password must be at least 6 characters"))
                    return@flow
                }
                request.password != request.confirmPassword -> {
                    emit(AuthResult.Error("Passwords do not match"))
                    return@flow
                }
            }
            
            // Create user in Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(request.email, request.password).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                // Update user profile with display name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName("${request.firstName} ${request.lastName}")
                    .build()
                
                firebaseUser.updateProfile(profileUpdates).await()
                
                // Send email verification
                firebaseUser.sendEmailVerification().await()
                
                // Create user document in Firestore with enhanced profile data
                val displayName = "${request.firstName} ${request.lastName}".trim()
                val userDataMap = mapOf(
                    "id" to firebaseUser.uid,
                    "email" to request.email,
                    "firstName" to request.firstName,
                    "lastName" to request.lastName,
                    "displayName" to displayName, // Add displayName for easier lookup
                    "name" to displayName, // Alternative field name
                    "profileImageUrl" to "",
                    "phoneNumber" to "",
                    "phone" to "", // Alternative field name
                    "location" to "",
                    "bio" to "Welcome to GigGO! I'm excited to work on amazing projects.",
                    "skills" to listOf<String>(), // Empty list initially
                    "completedGigs" to 0,
                    "rating" to 0.0,
                    "isVerified" to false,
                    "isEmailVerified" to false,
                    "createdAt" to Date().time
                )
                
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .set(userDataMap)
                    .await()
                
                emit(AuthResult.Success)
            } else {
                emit(AuthResult.Error("Failed to create user account"))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error(e.getFirebaseErrorMessage()))
        }
    }
    
    suspend fun signIn(request: LoginRequest): Flow<AuthResult> = flow {
        try {
            emit(AuthResult.Loading)
            
            // Validate input
            when {
                !request.email.isValidEmail() -> {
                    emit(AuthResult.Error("Invalid email format"))
                    return@flow
                }
                request.password.length < 6 -> {
                    emit(AuthResult.Error("Password must be at least 6 characters"))
                    return@flow
                }
            }
            
            // Sign in with Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(request.email, request.password).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                emit(AuthResult.Success)
            } else {
                emit(AuthResult.Error("Failed to sign in"))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error(e.getFirebaseErrorMessage()))
        }
    }
    
    suspend fun resetPassword(email: String): Flow<AuthResult> = flow {
        try {
            emit(AuthResult.Loading)
            
            if (!email.isValidEmail()) {
                emit(AuthResult.Error("Invalid email format"))
                return@flow
            }
            
            auth.sendPasswordResetEmail(email).await()
            emit(AuthResult.Success)
        } catch (e: Exception) {
            emit(AuthResult.Error(e.getFirebaseErrorMessage()))
        }
    }
    
    suspend fun getCurrentUser(): User? {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val userDoc = firestore.collection("users")
                    .document(firebaseUser.uid)
                    .get()
                    .await()
                
                userDoc.toObject(User::class.java)?.copy(
                    isEmailVerified = firebaseUser.isEmailVerified
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun signOut() {
        auth.signOut()
    }
    
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }
    
    suspend fun reloadUser(): Boolean {
        return try {
            auth.currentUser?.reload()?.await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun sendEmailVerification(): Boolean {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun updateUserProfile(firstName: String, lastName: String, phoneNumber: String): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            val userId = currentUser.uid
            
            // Update display name in Firebase Auth
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName("$firstName $lastName")
                .build()
            
            currentUser.updateProfile(profileUpdates).await()
            
            // Update user document in Firestore
            val userUpdates = mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "phoneNumber" to phoneNumber
            )
            
            firestore.collection("users")
                .document(userId)
                .update(userUpdates)
                .await()
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun String.isValidEmail(): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }
    
    private fun Exception.getFirebaseErrorMessage(): String {
        return when (this.message) {
            "The email address is already in use by another account." -> 
                "This email is already registered. Please use a different email or try signing in."
            "The password is invalid or the user does not have a password." -> 
                "Invalid email or password. Please check your credentials."
            "There is no user record corresponding to this identifier. The user may have been deleted." -> 
                "No account found with this email. Please check your email or sign up."
            "The email address is badly formatted." -> 
                "Please enter a valid email address."
            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> 
                "Network error. Please check your internet connection and try again."
            "We have blocked all requests from this device due to unusual activity. Try again later." -> 
                "Too many failed attempts. Please try again later."
            else -> this.message ?: "An unexpected error occurred. Please try again."
        }
    }
}
