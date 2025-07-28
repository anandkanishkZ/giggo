package com.natrajtechnology.giggo.data.repository

import com.natrajtechnology.giggo.data.firebase.FirebaseGigService
import com.natrajtechnology.giggo.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GigRepository @Inject constructor(
    private val firebaseGigService: FirebaseGigService
) {
    
    /**
     * Creates a new gig
     */
    suspend fun createGig(
        title: String,
        description: String,
        category: String,
        price: Double,
        deliveryTimeInDays: Int,
        requirements: String = "",
        tags: List<String> = emptyList()
    ): CreateGigResponse {
        val request = CreateGigRequest(
            title = title,
            description = description,
            category = category,
            price = price,
            deliveryTimeInDays = deliveryTimeInDays,
            requirements = requirements,
            tags = tags
        )
        return firebaseGigService.createGig(request)
    }

    /**
     * Saves gig as draft
     */
    suspend fun saveDraft(
        title: String,
        description: String,
        category: String,
        price: String,
        deliveryTime: String,
        requirements: String = ""
    ): SaveDraftResponse {
        val request = SaveDraftRequest(
            title = title,
            description = description,
            category = category,
            price = price,
            deliveryTime = deliveryTime,
            requirements = requirements
        )
        return firebaseGigService.saveDraft(request)
    }

    /**
     * Updates an existing draft
     */
    suspend fun updateDraft(
        draftId: String,
        title: String,
        description: String,
        category: String,
        price: String,
        deliveryTime: String,
        requirements: String = ""
    ): SaveDraftResponse {
        val request = SaveDraftRequest(
            title = title,
            description = description,
            category = category,
            price = price,
            deliveryTime = deliveryTime,
            requirements = requirements
        )
        return firebaseGigService.updateDraft(draftId, request)
    }

    /**
     * Gets all user gigs
     */
    suspend fun getUserGigs(): List<Gig> {
        return firebaseGigService.getUserGigs()
    }

    /**
     * Gets all user drafts
     */
    suspend fun getUserDrafts(): List<GigDraft> {
        return firebaseGigService.getUserDrafts()
    }

    /**
     * Deletes a draft
     */
    suspend fun deleteDraft(draftId: String): Boolean {
        return firebaseGigService.deleteDraft(draftId)
    }

    /**
     * Publishes a draft as a gig
     */
    suspend fun publishDraft(draftId: String): CreateGigResponse {
        return firebaseGigService.publishDraft(draftId)
    }

    /**
     * Gets a specific gig by ID
     */
    suspend fun getGigById(gigId: String): Gig? {
        return firebaseGigService.getGigById(gigId)
    }

    /**
     * Gets all published gigs by category
     */
    suspend fun getGigsByCategory(category: String): List<Gig> {
        return firebaseGigService.getGigsByCategory(category)
    }

    /**
     * Gets all published gigs
     */
    suspend fun getAllPublishedGigs(): List<Gig> {
        return firebaseGigService.getAllPublishedGigs()
    }

    /**
     * Updates gig status
     */
    suspend fun updateGigStatus(gigId: String, status: GigStatus): Boolean {
        return firebaseGigService.updateGigStatus(gigId, status)
    }

    /**
     * Debug - Gets all gigs without filters
     */
    suspend fun getAllGigsDebug(): List<Gig> {
        return firebaseGigService.getAllGigsForDebug()
    }
    
    /**
     * Simple method to get all gigs (for testing)
     */
    suspend fun getAllGigsSimple(): List<Gig> {
        return firebaseGigService.getAllGigsSimple()
    }
    
    /**
     * Fixes any data inconsistencies in the database
     */
    suspend fun fixDataInconsistencies() {
        try {
            com.natrajtechnology.giggo.admin.GigCategoryFixer.runFix()
        } catch (e: Exception) {
            println("Error fixing data inconsistencies: ${e.message}")
        }
    }
}
