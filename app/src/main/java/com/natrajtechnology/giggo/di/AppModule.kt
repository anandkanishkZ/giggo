package com.natrajtechnology.giggo.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.natrajtechnology.giggo.data.firebase.FirebaseAuthService
import com.natrajtechnology.giggo.data.firebase.FirebaseEmailService
import com.natrajtechnology.giggo.data.firebase.FirebaseGigService
import com.natrajtechnology.giggo.data.firebase.FirebaseNotificationService
import com.natrajtechnology.giggo.data.repository.AuthRepository
import com.natrajtechnology.giggo.data.repository.GigRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuthService(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): FirebaseAuthService = FirebaseAuthService(auth, firestore)

    @Provides
    @Singleton
    fun provideFirebaseGigService(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): FirebaseGigService = FirebaseGigService(firestore, auth)

    @Provides
    @Singleton
    fun provideFirebaseEmailService(): FirebaseEmailService = FirebaseEmailService()

    @Provides
    @Singleton
    fun provideFirebaseNotificationService(
        emailService: FirebaseEmailService
    ): FirebaseNotificationService = FirebaseNotificationService(emailService)

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuthService: FirebaseAuthService
    ): AuthRepository = AuthRepository(firebaseAuthService)

    @Provides
    @Singleton
    fun provideGigRepository(
        firebaseGigService: FirebaseGigService
    ): GigRepository = GigRepository(firebaseGigService)
}
