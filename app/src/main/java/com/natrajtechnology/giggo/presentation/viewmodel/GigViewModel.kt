package com.natrajtechnology.giggo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.natrajtechnology.giggo.data.model.*
import com.natrajtechnology.giggo.data.repository.GigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class GigUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val createdGigId: String? = null,
    val savedDraftId: String? = null
)

data class GigFormState(
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val price: String = "",
    val deliveryTime: String = "",
    val requirements: String = "",
    val isValid: Boolean = false
)

@HiltViewModel
class GigViewModel @Inject constructor(
    private val gigRepository: GigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GigUiState())
    val uiState: StateFlow<GigUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(GigFormState())
    val formState: StateFlow<GigFormState> = _formState.asStateFlow()

    private val _userGigs = MutableStateFlow<List<Gig>>(emptyList())
    val userGigs: StateFlow<List<Gig>> = _userGigs.asStateFlow()

    private val _userDrafts = MutableStateFlow<List<GigDraft>>(emptyList())
    val userDrafts: StateFlow<List<GigDraft>> = _userDrafts.asStateFlow()

    /**
     * Updates form field and validates the form
     */
    fun updateFormField(field: String, value: String) {
        val currentState = _formState.value
        val newState = when (field) {
            "title" -> currentState.copy(title = value)
            "description" -> currentState.copy(description = value)
            "category" -> currentState.copy(category = value)
            "price" -> currentState.copy(price = value)
            "deliveryTime" -> currentState.copy(deliveryTime = value)
            "requirements" -> currentState.copy(requirements = value)
            else -> currentState
        }
        
        _formState.value = newState.copy(isValid = validateForm(newState))
    }

    /**
     * Validates the form for gig creation
     */
    private fun validateForm(state: GigFormState): Boolean {
        return state.title.isNotBlank() &&
                state.description.isNotBlank() &&
                state.category.isNotBlank() &&
                state.price.isNotBlank() &&
                state.deliveryTime.isNotBlank() &&
                state.price.toDoubleOrNull() != null &&
                state.price.toDoubleOrNull()!! > 0 &&
                state.deliveryTime.toIntOrNull() != null &&
                state.deliveryTime.toIntOrNull()!! > 0
    }

    /**
     * Creates a new gig
     */
    fun createGig() {
        val formState = _formState.value
        
        println("Debug: GigViewModel - createGig() called")
        println("Debug: GigViewModel - Form state: $formState")
        
        if (!formState.isValid) {
            val errorMsg = "Please fill all required fields correctly"
            println("Debug: GigViewModel - Form validation failed: $errorMsg")
            _uiState.value = _uiState.value.copy(
                errorMessage = errorMsg
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            println("Debug: GigViewModel - Starting gig creation process")

            try {
                val price = formState.price.toDoubleOrNull() ?: 0.0
                val deliveryTime = formState.deliveryTime.toIntOrNull() ?: 0

                println("Debug: GigViewModel - Parsed values - price: $price, deliveryTime: $deliveryTime")

                val result = gigRepository.createGig(
                    title = formState.title,
                    description = formState.description,
                    category = formState.category,
                    price = price,
                    deliveryTimeInDays = deliveryTime,
                    requirements = formState.requirements
                )

                println("Debug: GigViewModel - Repository result: success=${result.success}, message=${result.message}")

                if (result.success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        successMessage = result.message,
                        createdGigId = result.gigId
                    )
                    clearForm()
                    loadUserGigs() // Refresh gigs list
                    println("Debug: GigViewModel - Gig created successfully with ID: ${result.gigId}")
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                    println("Debug: GigViewModel - Gig creation failed: ${result.message}")
                }
            } catch (e: Exception) {
                val errorMsg = "Failed to create gig: ${e.message}"
                println("Debug: GigViewModel - Exception during gig creation: $errorMsg")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
            }
        }
    }

    /**
     * Saves gig as draft
     */
    fun saveDraft() {
        val formState = _formState.value
        
        // For draft, we only require title to be filled
        if (formState.title.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Title is required to save as draft"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val result = gigRepository.saveDraft(
                    title = formState.title,
                    description = formState.description,
                    category = formState.category,
                    price = formState.price,
                    deliveryTime = formState.deliveryTime,
                    requirements = formState.requirements
                )

                if (result.success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        successMessage = result.message,
                        savedDraftId = result.draftId
                    )
                    clearForm()
                    loadUserDrafts() // Refresh drafts list
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to save draft: ${e.message}"
                )
            }
        }
    }

    /**
     * Loads user's gigs
     */
    fun loadUserGigs() {
        viewModelScope.launch {
            try {
                println("Debug: ViewModel - Loading user gigs...")
                val gigs = gigRepository.getUserGigs()
                println("Debug: ViewModel - Received ${gigs.size} gigs")
                _userGigs.value = gigs
            } catch (e: Exception) {
                println("Debug: ViewModel - Error loading gigs: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load gigs: ${e.message}"
                )
            }
        }
    }

    /**
     * Loads user's drafts
     */
    fun loadUserDrafts() {
        viewModelScope.launch {
            try {
                val drafts = gigRepository.getUserDrafts()
                _userDrafts.value = drafts
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load drafts: ${e.message}"
                )
            }
        }
    }

    /**
     * Publishes a draft as a gig
     */
    fun publishDraft(draftId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val result = gigRepository.publishDraft(draftId)

                if (result.success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        successMessage = result.message,
                        createdGigId = result.gigId
                    )
                    loadUserGigs()
                    loadUserDrafts()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to publish draft: ${e.message}"
                )
            }
        }
    }

    /**
     * Deletes a draft
     */
    fun deleteDraft(draftId: String) {
        viewModelScope.launch {
            try {
                val success = gigRepository.deleteDraft(draftId)
                if (success) {
                    loadUserDrafts()
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Draft deleted successfully"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to delete draft"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to delete draft: ${e.message}"
                )
            }
        }
    }

    /**
     * Clears the form
     */
    fun clearForm() {
        _formState.value = GigFormState()
    }

    /**
     * Clears error and success messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null,
            isSuccess = false
        )
    }

    /**
     * Sets form state (useful for editing drafts)
     */
    fun setFormState(
        title: String = "",
        description: String = "",
        category: String = "",
        price: String = "",
        deliveryTime: String = "",
        requirements: String = ""
    ) {
        val newState = GigFormState(
            title = title,
            description = description,
            category = category,
            price = price,
            deliveryTime = deliveryTime,
            requirements = requirements
        )
        _formState.value = newState.copy(isValid = validateForm(newState))
    }

    /**
     * Creates a test gig for debugging purposes
     */
    fun createTestGig() {
        println("Debug: GigViewModel - Creating test gig")
        
        // Set test form data
        _formState.value = GigFormState(
            title = "Test Android Development Service",
            description = "I will create a professional Android app for your business. This includes UI/UX design, development, testing, and deployment to Google Play Store.",
            category = "Mobile Development",
            price = "5000",
            deliveryTime = "7",
            requirements = "Please provide your app requirements, target audience, and any specific features you need.",
            isValid = true
        )
        
        // Create the gig
        createGig()
    }

    /**
     * Debug function to check authentication and permissions
     */
    fun debugGigCreation() {
        viewModelScope.launch {
            try {
                println("=== GIG CREATION DEBUG ===")
                
                // Check authentication
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    println("Debug: User authenticated - ID: ${currentUser.uid}, Email: ${currentUser.email}")
                    
                    // Test Firestore connection
                    try {
                        val testDoc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("test")
                            .document("gig_debug_${System.currentTimeMillis()}")
                        
                        testDoc.set(mapOf(
                            "test" to "gig_creation_debug",
                            "userId" to currentUser.uid,
                            "timestamp" to com.google.firebase.Timestamp.now()
                        )).await()
                        
                        println("Debug: Firestore connection working - test document created")
                        
                        // Clean up test document
                        testDoc.delete().await()
                        println("Debug: Test document cleaned up")
                        
                    } catch (e: Exception) {
                        println("Debug: Firestore connection error: ${e.message}")
                        e.printStackTrace()
                    }
                    
                } else {
                    println("Debug: User NOT authenticated")
                }
                
                // Check form validation
                val formState = _formState.value
                println("Debug: Form state - Valid: ${formState.isValid}")
                println("Debug: Title: '${formState.title}' (length: ${formState.title.length})")
                println("Debug: Description: '${formState.description}' (length: ${formState.description.length})")
                println("Debug: Category: '${formState.category}'")
                println("Debug: Price: '${formState.price}' (parsed: ${formState.price.toDoubleOrNull()})")
                println("Debug: Delivery Time: '${formState.deliveryTime}' (parsed: ${formState.deliveryTime.toIntOrNull()})")
                
                println("=== END GIG DEBUG ===")
                
            } catch (e: Exception) {
                println("Debug: Error in debug function: ${e.message}")
                e.printStackTrace()
            }
        }
    }

}
