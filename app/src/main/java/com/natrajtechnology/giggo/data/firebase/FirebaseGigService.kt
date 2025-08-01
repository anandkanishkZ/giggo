package com.natrajtechnology.giggo.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import com.natrajtechnology.giggo.data.model.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseGigService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val gigsCollection = firestore.collection("gigs")
    private val draftsCollection = firestore.collection("gig_drafts")

    /**
     * Creates a new gig and publishes it
     */
    suspend fun createGig(request: CreateGigRequest): CreateGigResponse {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                println("Debug: CreateGig - User not authenticated")
                return CreateGigResponse(false, message = "User not authenticated. Please sign in and try again.")
            }

            println("Debug: CreateGig - User authenticated: ${currentUser.uid} (${currentUser.email})")

            // Validate input
            if (request.title.isBlank()) {
                return CreateGigResponse(false, message = "Title cannot be empty")
            }
            if (request.description.isBlank()) {
                return CreateGigResponse(false, message = "Description cannot be empty")
            }
            if (request.category.isBlank()) {
                return CreateGigResponse(false, message = "Category must be selected")
            }
            if (request.price <= 0) {
                return CreateGigResponse(false, message = "Price must be greater than 0")
            }
            if (request.deliveryTimeInDays <= 0) {
                return CreateGigResponse(false, message = "Delivery time must be greater than 0")
            }

            println("Debug: CreateGig - Validation passed")
            println("Debug: CreateGig - Request: title='${request.title}', category='${request.category}', price=${request.price}")

            val gigId = gigsCollection.document().id
            
            // Create gig data as Map to ensure proper Firestore serialization
            val gigData = mapOf(
                "id" to gigId,
                "userId" to currentUser.uid,
                "title" to request.title.trim(),
                "description" to request.description.trim(),
                "category" to request.category,
                "price" to request.price,
                "deliveryTimeInDays" to request.deliveryTimeInDays,
                "requirements" to request.requirements.trim(),
                "status" to "PUBLISHED", // Store as string consistently
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now(),
                "tags" to request.tags,
                "isActive" to true
            )
            
            println("Debug: CreateGig - Creating gig with ID: $gigId")
            println("Debug: CreateGig - Gig data: $gigData")

            // Attempt to save to Firestore
            try {
                gigsCollection.document(gigId).set(gigData).await()
                println("Debug: CreateGig - Firestore set operation completed")
            } catch (firestoreException: Exception) {
                println("Debug: CreateGig - Firestore error: ${firestoreException.message}")
                firestoreException.printStackTrace()
                return CreateGigResponse(
                    success = false,
                    message = "Database error: ${firestoreException.message}. Please check your internet connection and try again."
                )
            }
            
            // Verify the gig was saved
            try {
                val savedDoc = gigsCollection.document(gigId).get().await()
                if (savedDoc.exists()) {
                    println("Debug: CreateGig - Gig successfully saved to Firestore")
                    println("Debug: CreateGig - Saved gig data: ${savedDoc.data}")
                    return CreateGigResponse(
                        success = true,
                        gigId = gigId,
                        message = "Gig created successfully! 🎉"
                    )
                } else {
                    println("Debug: CreateGig - ERROR - Gig document does not exist after creation")
                    return CreateGigResponse(
                        success = false,
                        message = "Failed to save gig data. Please try again."
                    )
                }
            } catch (verificationException: Exception) {
                println("Debug: CreateGig - Error verifying gig creation: ${verificationException.message}")
                // The gig might still have been created, so return success but with a warning
                return CreateGigResponse(
                    success = true,
                    gigId = gigId,
                    message = "Gig created successfully! 🎉"
                )
            }

        } catch (e: Exception) {
            println("Debug: CreateGig - Unexpected error: ${e.message}")
            e.printStackTrace()
            CreateGigResponse(
                success = false,
                message = "Failed to create gig: ${e.message}. Please try again."
            )
        }
    }

    /**
     * Saves gig as draft
     */
    suspend fun saveDraft(request: SaveDraftRequest): SaveDraftResponse {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return SaveDraftResponse(false, message = "User not authenticated")
            }

            val draftId = draftsCollection.document().id
            val draft = GigDraft(
                id = draftId,
                userId = currentUser.uid,
                title = request.title.trim(),
                description = request.description.trim(),
                category = request.category,
                price = request.price,
                deliveryTime = request.deliveryTime,
                requirements = request.requirements.trim(),
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            draftsCollection.document(draftId).set(draft).await()

            SaveDraftResponse(
                success = true,
                draftId = draftId,
                message = "Draft saved successfully!"
            )
        } catch (e: Exception) {
            SaveDraftResponse(
                success = false,
                message = "Failed to save draft: ${e.message}"
            )
        }
    }

    /**
     * Gets all gigs for the current user
     */
    suspend fun getUserGigs(): List<Gig> {
        return try {
            val currentUser = auth.currentUser ?: return emptyList()
            
            println("Debug: Getting gigs for user: ${currentUser.uid}")
            println("Debug: User email: ${currentUser.email}")
            
            val snapshot = gigsCollection
                .whereEqualTo("userId", currentUser.uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val gigs = snapshot.documents.mapNotNull { doc ->
                val gig = doc.toObject(Gig::class.java)
                println("Debug: Document ${doc.id} - userId: ${gig?.userId}, title: ${gig?.title}")
                gig
            }
            
            println("Debug: Found ${gigs.size} gigs for user ${currentUser.uid}")
            
            // Also try to get all gigs to see what's in the database
            val allSnapshot = gigsCollection.get().await()
            println("Debug: Total gigs in database: ${allSnapshot.documents.size}")
            allSnapshot.documents.forEach { doc ->
                val gig = doc.toObject(Gig::class.java)
                println("Debug: All Gigs - ID: ${doc.id}, userId: ${gig?.userId}, title: ${gig?.title}")
            }
            
            gigs
        } catch (e: Exception) {
            println("Debug: Error loading user gigs: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Gets all drafts for the current user
     */
    suspend fun getUserDrafts(): List<GigDraft> {
        return try {
            val currentUser = auth.currentUser ?: return emptyList()
            
            val snapshot = draftsCollection
                .whereEqualTo("userId", currentUser.uid)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(GigDraft::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Updates an existing draft
     */
    suspend fun updateDraft(draftId: String, request: SaveDraftRequest): SaveDraftResponse {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return SaveDraftResponse(false, message = "User not authenticated")
            }

            val updatedDraft = mapOf(
                "title" to request.title.trim(),
                "description" to request.description.trim(),
                "category" to request.category,
                "price" to request.price,
                "deliveryTime" to request.deliveryTime,
                "requirements" to request.requirements.trim(),
                "updatedAt" to Timestamp.now()
            )

            draftsCollection.document(draftId).update(updatedDraft).await()

            SaveDraftResponse(
                success = true,
                draftId = draftId,
                message = "Draft updated successfully!"
            )
        } catch (e: Exception) {
            SaveDraftResponse(
                success = false,
                message = "Failed to update draft: ${e.message}"
            )
        }
    }

    /**
     * Deletes a draft
     */
    suspend fun deleteDraft(draftId: String): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            draftsCollection.document(draftId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Publishes a draft as a gig
     */
    suspend fun publishDraft(draftId: String): CreateGigResponse {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return CreateGigResponse(false, message = "User not authenticated")
            }

            // Get draft
            val draftDoc = draftsCollection.document(draftId).get().await()
            val draft = draftDoc.toObject(GigDraft::class.java)
                ?: return CreateGigResponse(false, message = "Draft not found")

            // Validate draft data
            val price = draft.price.toDoubleOrNull()
            val deliveryTime = draft.deliveryTime.toIntOrNull()

            if (price == null || price <= 0) {
                return CreateGigResponse(false, message = "Invalid price format")
            }
            if (deliveryTime == null || deliveryTime <= 0) {
                return CreateGigResponse(false, message = "Invalid delivery time format")
            }

            // Create gig from draft
            val createRequest = CreateGigRequest(
                title = draft.title,
                description = draft.description,
                category = draft.category,
                price = price,
                deliveryTimeInDays = deliveryTime,
                requirements = draft.requirements
            )

            val result = createGig(createRequest)
            
            // If successful, delete the draft
            if (result.success) {
                deleteDraft(draftId)
            }

            result
        } catch (e: Exception) {
            CreateGigResponse(
                success = false,
                message = "Failed to publish draft: ${e.message}"
            )
        }
    }

    /**
     * Gets a specific gig by ID
     */
    suspend fun getGigById(gigId: String): Gig? {
        return try {
            println("Debug: FirebaseGigService - getGigById called with ID: '$gigId'")
            
            if (gigId.isBlank()) {
                println("Debug: FirebaseGigService - ERROR: GigId is blank!")
                return null
            }
            
            val document = gigsCollection.document(gigId).get().await()
            println("Debug: FirebaseGigService - Document exists: ${document.exists()}")
            
            if (document.exists()) {
                val gig = document.toObject(Gig::class.java)
                println("Debug: FirebaseGigService - Gig object created: ${gig != null}")
                if (gig != null) {
                    println("Debug: FirebaseGigService - Gig details - Title: '${gig.title}', Category: '${gig.category}'")
                }
                gig
            } else {
                println("Debug: FirebaseGigService - Document does not exist for ID: '$gigId'")
                null
            }
        } catch (e: Exception) {
            println("Debug: FirebaseGigService - Exception in getGigById: ${e.message}")
            println("Debug: FirebaseGigService - Exception type: ${e::class.simpleName}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Gets all published gigs by category
     */
    suspend fun getGigsByCategory(category: String): List<Gig> {
        return try {
            println("Debug: === STARTING getGigsByCategory for: $category ===")
            println("Debug: Current user: ${auth.currentUser?.uid}")
            
            // First check if we have any gigs at all
            val allSnapshot = gigsCollection.get().await()
            println("Debug: Total documents in collection: ${allSnapshot.documents.size}")
            
            if (allSnapshot.documents.isEmpty()) {
                println("Debug: ❌ NO DOCUMENTS FOUND IN GIGS COLLECTION!")
                return emptyList()
            }
            
            // Check how many documents match our category
            val categoryCount = allSnapshot.documents.count { doc ->
                val docCategory = doc.getString("category")
                docCategory == category
            }
            println("Debug: Documents with category '$category': $categoryCount")
            
            // Check status and isActive fields in category documents
            allSnapshot.documents.filter { doc ->
                doc.getString("category") == category
            }.forEachIndexed { index, doc ->
                val status = doc.get("status")
                val isActive = doc.getBoolean("isActive")
                val title = doc.getString("title")
                println("Debug: Category Doc $index - Title: '$title', Status: $status (${status?.javaClass?.simpleName}), Active: $isActive")
            }
            
            // Try the full filtered query
            println("Debug: Step 1 - Trying full filtered query...")
            val snapshot = gigsCollection
                .whereEqualTo("category", category)
                .whereEqualTo("status", "PUBLISHED") // Query as string consistently
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            println("Debug: Full filtered query found ${snapshot.documents.size} documents for category $category")
            
            if (snapshot.documents.isEmpty()) {
                println("Debug: ⚠️ Full filtered query returned no results. Trying category + active only...")
                
                // Try query with just category and isActive
                val categoryActiveSnapshot = gigsCollection
                    .whereEqualTo("category", category)
                    .whereEqualTo("isActive", true)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                println("Debug: Category + Active query found ${categoryActiveSnapshot.documents.size} documents")
                
                if (categoryActiveSnapshot.documents.isNotEmpty()) {
                    val categoryActiveGigs = categoryActiveSnapshot.documents.mapNotNull { doc ->
                        try {
                            val gig = doc.toObject(Gig::class.java)
                            println("Debug: Category+Active Gig - Title: ${gig?.title}, Status: '${gig?.status}', Active: ${gig?.isActive}")
                            gig
                        } catch (e: Exception) {
                            println("Debug: Error parsing category+active gig ${doc.id}: ${e.message}")
                            null
                        }
                    }
                    println("Debug: Returning ${categoryActiveGigs.size} category+active gigs")
                    return categoryActiveGigs
                }
                
                // If still no results, try just category
                println("Debug: ⚠️ Trying category only query...")
                val categoryOnlySnapshot = gigsCollection
                    .whereEqualTo("category", category)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                println("Debug: Category only query found ${categoryOnlySnapshot.documents.size} documents")
                
                if (categoryOnlySnapshot.documents.isNotEmpty()) {
                    val categoryOnlyGigs = categoryOnlySnapshot.documents.mapNotNull { doc ->
                        try {
                            val gig = doc.toObject(Gig::class.java)
                            println("Debug: Category-only Gig - Title: ${gig?.title}, Status: '${gig?.status}', Active: ${gig?.isActive}")
                            gig
                        } catch (e: Exception) {
                            println("Debug: Error parsing category-only gig ${doc.id}: ${e.message}")
                            null
                        }
                    }
                    println("Debug: Returning ${categoryOnlyGigs.size} category-only gigs")
                    return categoryOnlyGigs
                }
            }
            
            val gigs = snapshot.documents.mapNotNull { doc ->
                try {
                    val gig = doc.toObject(Gig::class.java)
                    println("Debug: Category Gig - Title: ${gig?.title}, Status: '${gig?.status}', Category: ${gig?.category}")
                    gig
                } catch (e: Exception) {
                    println("Debug: Error parsing category gig ${doc.id}: ${e.message}")
                    null
                }
            }
            
            println("Debug: === FINAL RESULT for $category: ${gigs.size} gigs ===")
            gigs
        } catch (e: Exception) {
            println("Debug: ❌ ERROR fetching gigs for category $category: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Gets all published gigs
     */
    suspend fun getAllPublishedGigs(): List<Gig> {
        return try {
            println("Debug: === STARTING getAllPublishedGigs ===")
            println("Debug: Current user: ${auth.currentUser?.uid}")
            println("Debug: Current user email: ${auth.currentUser?.email}")
            
            // First, let's try a simple query without any filters
            println("Debug: Step 1 - Getting all documents...")
            val allSnapshot = gigsCollection.get().await()
            println("Debug: Total documents in gigs collection: ${allSnapshot.documents.size}")
            
            if (allSnapshot.documents.isEmpty()) {
                println("Debug: ❌ NO DOCUMENTS FOUND IN GIGS COLLECTION!")
                return emptyList()
            }
            
            // Check each document structure
            allSnapshot.documents.forEachIndexed { index, doc ->
                val data = doc.data
                val status = doc.get("status")
                val isActive = doc.getBoolean("isActive")
                val title = doc.getString("title")
                println("Debug: Doc $index - ID: ${doc.id}")
                println("Debug: Doc $index - Title: '$title'")
                println("Debug: Doc $index - Status: $status (Type: ${status?.javaClass?.simpleName})")
                println("Debug: Doc $index - IsActive: $isActive")
                println("Debug: Doc $index - Full data: $data")
                println("Debug: ---")
            }
            
            // Now try the filtered query
            println("Debug: Step 2 - Querying with filters...")
            val snapshot = gigsCollection
                .whereEqualTo("status", "PUBLISHED") // Query as string consistently
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            println("Debug: Filtered query found ${snapshot.documents.size} documents")
            
            if (snapshot.documents.isEmpty()) {
                println("Debug: ⚠️ Filtered query returned no results. Trying without status filter...")
                
                // Try query with just isActive filter
                val activeSnapshot = gigsCollection
                    .whereEqualTo("isActive", true)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                println("Debug: Active-only query found ${activeSnapshot.documents.size} documents")
                
                if (activeSnapshot.documents.isNotEmpty()) {
                    val activeGigs = activeSnapshot.documents.mapNotNull { doc ->
                        try {
                            val gig = doc.toObject(Gig::class.java)
                            println("Debug: Active Gig - Title: ${gig?.title}, Status: '${gig?.status}', Active: ${gig?.isActive}")
                            gig
                        } catch (e: Exception) {
                            println("Debug: Error parsing active gig ${doc.id}: ${e.message}")
                            null
                        }
                    }
                    println("Debug: Returning ${activeGigs.size} active gigs instead")
                    return activeGigs
                }
            }
            
            val gigs = snapshot.documents.mapNotNull { doc ->
                try {
                    val gig = doc.toObject(Gig::class.java)
                    println("Debug: Published Gig - Title: ${gig?.title}, Status: '${gig?.status}', Active: ${gig?.isActive}")
                    gig
                } catch (e: Exception) {
                    println("Debug: Error parsing gig ${doc.id}: ${e.message}")
                    null
                }
            }
            
            println("Debug: === FINAL RESULT: ${gigs.size} published gigs ===")
            gigs
        } catch (e: Exception) {
            println("Debug: ❌ ERROR in getAllPublishedGigs: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Debug method - Gets all gigs without any filters
     */
    suspend fun getAllGigsDebug(): List<Gig> {
        return try {
            println("Debug: Getting ALL gigs without any filters")
            val snapshot = gigsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            println("Debug: Found ${snapshot.documents.size} total documents")
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    val gig = doc.toObject(Gig::class.java)
                    println("Debug: Gig parsed - ${gig?.title} | Status: ${gig?.status} | Active: ${gig?.isActive}")
                    gig
                } catch (e: Exception) {
                    println("Debug: Error parsing document ${doc.id}: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            println("Debug: Error in getAllGigsDebug: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Simple method to get all gigs without status filter (for testing)
     */
    suspend fun getAllGigsSimple(): List<Gig> {
        return try {
            println("Debug: Getting all gigs with simple query")
            val snapshot = gigsCollection.get().await()
            println("Debug: Simple query found ${snapshot.documents.size} documents")
            
            val gigs = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Gig::class.java)
            }
            
            println("Debug: Simple query parsed ${gigs.size} gigs")
            gigs
        } catch (e: Exception) {
            println("Debug: Error in getAllGigsSimple: ${e.message}")
            emptyList()
        }
    }

    /**
     * Updates gig status
     */
    suspend fun updateGigStatus(gigId: String, status: GigStatus): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            
            gigsCollection.document(gigId).update(
                mapOf(
                    "status" to status.name, // Store as string consistently
                    "updatedAt" to Timestamp.now()
                )
            ).await()
            
            true
        } catch (e: Exception) {
            println("Debug: Error updating gig status: ${e.message}")
            false
        }
    }

    /**
     * Debug method to get all gigs in the database
     */
    suspend fun getAllGigsForDebug(): List<Gig> {
        return try {
            val currentUser = auth.currentUser
            println("Debug getAllGigs: Current user: ${currentUser?.uid}")
            println("Debug getAllGigs: User email: ${currentUser?.email}")
            
            val snapshot = gigsCollection.get().await()
            println("Debug getAllGigs: Total documents: ${snapshot.documents.size}")
            
            val allGigs = snapshot.documents.mapNotNull { doc ->
                val gigData = doc.data
                println("Debug getAllGigs: Doc ID: ${doc.id}")
                println("Debug getAllGigs: Doc Data: $gigData")
                
                val gig = doc.toObject(Gig::class.java)
                println("Debug getAllGigs: Gig userId: '${gig?.userId}'")
                println("Debug getAllGigs: Current user: '${currentUser?.uid}'")
                println("Debug getAllGigs: UserIds match: ${gig?.userId == currentUser?.uid}")
                println("Debug getAllGigs: Gig title: ${gig?.title}")
                println("Debug getAllGigs: Gig status: ${gig?.status}")
                println("Debug getAllGigs: Gig category: ${gig?.category}")
                println("Debug getAllGigs: Gig price: ${gig?.price}")
                println("Debug getAllGigs: Gig isActive: ${gig?.isActive}")
                println("Debug getAllGigs: ---")
                gig
            }
            
            println("Debug getAllGigs: Total parsed gigs: ${allGigs.size}")
            allGigs
        } catch (e: Exception) {
            println("Debug getAllGigs: Error: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
}
