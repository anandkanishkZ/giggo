// Admin utility to fix gig category mismatches and status inconsistencies in Firestore
// Usage: Run this as a one-time migration or from a debug/admin screen

package com.natrajtechnology.giggo.admin

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.runBlocking

object GigCategoryFixer {
    private val db = FirebaseFirestore.getInstance()
    private val allowedCategories = listOf(
        "Web Development", "Mobile Development", "UI/UX Design", "Graphic Design",
        "Content Writing", "Digital Marketing", "Data Entry", "Translation",
        "Video Editing", "Photography", "Music & Audio", "Programming & Tech",
        "Business", "Lifestyle"
    )

    fun runFix() = runBlocking {
        val gigs = db.collection("gigs").get().await()
        var fixedCount = 0
        
        println("=== GIG DATA FIX UTILITY ===")
        println("Found ${gigs.documents.size} gigs to analyze")
        
        for (doc in gigs.documents) {
            val gigId = doc.id
            val category = doc.getString("category")?.trim() ?: ""
            val status = doc.get("status")
            val isActive = doc.getBoolean("isActive")
            
            println("\n--- Processing Gig: $gigId ---")
            println("Category: '$category'")
            println("Status: $status (type: ${status?.javaClass?.simpleName})")
            println("IsActive: $isActive")
            
            val updates = mutableMapOf<String, Any>()
            
            // Fix status to be string consistently
            when (status) {
                is Map<*, *> -> {
                    // Status is stored as enum object, convert to string
                    val statusName = status["name"] as? String ?: "PUBLISHED"
                    updates["status"] = statusName
                    println("Fixed status from enum object to string: $statusName")
                }
                null -> {
                    updates["status"] = "PUBLISHED"
                    println("Fixed missing status to: PUBLISHED")
                }
                !is String -> {
                    updates["status"] = "PUBLISHED"
                    println("Fixed non-string status to: PUBLISHED")
                }
            }
            
            // Fix isActive field
            if (isActive == null) {
                updates["isActive"] = true
                println("Fixed missing isActive to: true")
            }
            
            // Log category issues (but don't auto-fix)
            if (category !in allowedCategories && category.isNotEmpty()) {
                println("WARNING: Invalid category '$category' (not auto-fixing)")
            }
            
            // Apply updates if any
            if (updates.isNotEmpty()) {
                try {
                    db.collection("gigs").document(gigId).update(updates).await()
                    fixedCount++
                    println("✅ Successfully updated gig $gigId")
                } catch (e: Exception) {
                    println("❌ Failed to update gig $gigId: ${e.message}")
                }
            } else {
                println("✅ No fixes needed for gig $gigId")
            }
        }
        
        println("\n=== FIX COMPLETE ===")
        println("Total gigs processed: ${gigs.documents.size}")
        println("Total gigs fixed: $fixedCount")
        
        // Verify the fixes
        println("\n=== VERIFICATION ===")
        val verifyGigs = db.collection("gigs").get().await()
        verifyGigs.documents.forEach { doc ->
            val status = doc.getString("status")
            val isActive = doc.getBoolean("isActive")
            val title = doc.getString("title")
            println("Gig: '$title' | Status: '$status' | Active: $isActive")
        }
    }
}

// To run: GigCategoryFixer.runFix() (from main or admin/debug entrypoint)
