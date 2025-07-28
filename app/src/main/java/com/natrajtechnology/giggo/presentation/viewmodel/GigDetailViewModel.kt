package com.natrajtechnology.giggo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.natrajtechnology.giggo.data.model.Gig
import com.natrajtechnology.giggo.data.repository.GigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GigDetailUiState(
    val isLoading: Boolean = false,
    val gig: Gig? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class GigDetailViewModel @Inject constructor(
    private val gigRepository: GigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GigDetailUiState())
    val uiState: StateFlow<GigDetailUiState> = _uiState.asStateFlow()

    /**
     * Loads gig details by ID
     */
    fun loadGigDetails(gigId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                println("Debug: GigDetailViewModel - Loading gig details for ID: '$gigId'")
                
                if (gigId.isBlank()) {
                    println("Debug: GigDetailViewModel - ERROR: GigId is blank or empty!")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Invalid gig ID"
                    )
                    return@launch
                }
                
                val gig = gigRepository.getGigById(gigId)
                
                if (gig != null) {
                    println("Debug: GigDetailViewModel - Gig loaded successfully: ${gig.title}")
                    println("Debug: GigDetailViewModel - Gig details - ID: ${gig.id}, Category: ${gig.category}, Price: ${gig.price}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        gig = gig
                    )
                } else {
                    println("Debug: GigDetailViewModel - Gig not found for ID: '$gigId'")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Gig not found"
                    )
                }
            } catch (e: Exception) {
                println("Debug: GigDetailViewModel - Error loading gig details: ${e.message}")
                println("Debug: GigDetailViewModel - Exception type: ${e::class.simpleName}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load gig details: ${e.message}"
                )
            }
        }
    }

    /**
     * Clears error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
