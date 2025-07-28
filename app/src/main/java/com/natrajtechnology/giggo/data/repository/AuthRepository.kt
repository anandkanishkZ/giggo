package com.natrajtechnology.giggo.data.repository

import com.natrajtechnology.giggo.data.firebase.FirebaseAuthService
import com.natrajtechnology.giggo.data.model.AuthResult
import com.natrajtechnology.giggo.data.model.LoginRequest
import com.natrajtechnology.giggo.data.model.SignUpRequest
import com.natrajtechnology.giggo.data.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuthService: FirebaseAuthService
) {
    
    suspend fun login(request: LoginRequest): Flow<AuthResult> {
        return firebaseAuthService.signIn(request)
    }
    
    suspend fun signUp(request: SignUpRequest): Flow<AuthResult> {
        return firebaseAuthService.signUp(request)
    }
    
    suspend fun resetPassword(email: String): Flow<AuthResult> {
        return firebaseAuthService.resetPassword(email)
    }
    
    suspend fun getCurrentUser(): User? {
        return firebaseAuthService.getCurrentUser()
    }
    
    suspend fun logout() {
        firebaseAuthService.signOut()
    }
    
    fun isUserSignedIn(): Boolean {
        return firebaseAuthService.isUserSignedIn()
    }
    
    suspend fun reloadUser(): Boolean {
        return firebaseAuthService.reloadUser()
    }
    
    suspend fun sendEmailVerification(): Boolean {
        return firebaseAuthService.sendEmailVerification()
    }
    
    suspend fun updateUserProfile(firstName: String, lastName: String, phoneNumber: String): Boolean {
        return firebaseAuthService.updateUserProfile(firstName, lastName, phoneNumber)
    }
}
