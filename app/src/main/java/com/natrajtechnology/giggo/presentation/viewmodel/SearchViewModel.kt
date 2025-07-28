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

data class SearchUiState(
    val isLoading: Boolean = false,
    val searchResults: List<Gig> = emptyList(),
    val categoryGigs: List<Gig> = emptyList(),
    val allGigs: List<Gig> = emptyList(),
    val errorMessage: String? = null,
    val selectedCategory: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val gigRepository: GigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadAllPublishedGigs()
        debugAllGigs()
    }

    /**
     * Debug method to check all gigs in database
     */
    private fun debugAllGigs() {
        viewModelScope.launch {
            try {
                val allGigs = gigRepository.getAllGigsDebug()
                println("Debug: Total gigs in database: ${allGigs.size}")
                allGigs.forEach { gig ->
                    println("Debug: DB Gig - ${gig.title} | Category: ${gig.category} | Status: ${gig.status} | Active: ${gig.isActive}")
                }
            } catch (e: Exception) {
                println("Debug: Error loading all gigs: ${e.message}")
            }
        }
    }

    /**
     * Updates search query and performs search
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        performSearch(query)
    }

    /**
     * Performs search across all gigs
     */
    private fun performSearch(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                selectedCategory = null
            )
            return
        }

        viewModelScope.launch {
            val allGigs = _uiState.value.allGigs
            val results = allGigs.filter { gig ->
                gig.title.contains(query, ignoreCase = true) ||
                gig.description.contains(query, ignoreCase = true) ||
                gig.category.contains(query, ignoreCase = true) ||
                gig.tags.any { tag -> tag.contains(query, ignoreCase = true) }
            }
            
            _uiState.value = _uiState.value.copy(
                searchResults = results,
                selectedCategory = null
            )
        }
    }

    /**
     * Gets gigs by category
     */
    fun getGigsByCategory(category: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                selectedCategory = category
            )

            try {
                println("Debug: SearchViewModel - Getting gigs for category: $category")
                val gigs = gigRepository.getGigsByCategory(category)
                println("Debug: SearchViewModel - Fetched ${gigs.size} gigs for category: $category")
                
                if (gigs.isEmpty()) {
                    println("Debug: SearchViewModel - No gigs found for category $category, checking all gigs...")
                    val allGigs = gigRepository.getAllGigsSimple()
                    val categoryGigsFromAll = allGigs.filter { it.category == category }
                    println("Debug: SearchViewModel - Found ${categoryGigsFromAll.size} gigs for category $category from all gigs")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categoryGigs = categoryGigsFromAll,
                        searchResults = emptyList(),
                        errorMessage = if (categoryGigsFromAll.isEmpty()) "No gigs found in $category category" else null
                    )
                } else {
                    gigs.forEach { gig ->
                        println("Debug: SearchViewModel - Category Gig: ${gig.title} | Category: ${gig.category} | Status: ${gig.status}")
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categoryGigs = gigs,
                        searchResults = emptyList()
                    )
                }
                
                // Clear search query when selecting category
                _searchQuery.value = ""
            } catch (e: Exception) {
                println("Debug: SearchViewModel - Error fetching gigs for category $category: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load category gigs: ${e.message}"
                )
            }
        }
    }

    /**
     * Loads all published gigs
     */
    private fun loadAllPublishedGigs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                println("Debug: SearchViewModel - Starting to load all published gigs")
                
                // Try simple query first
                val simpleGigs = gigRepository.getAllGigsSimple()
                println("Debug: SearchViewModel - Simple query returned ${simpleGigs.size} gigs")
                
                // Then try the filtered query
                val gigs = gigRepository.getAllPublishedGigs()
                println("Debug: SearchViewModel - Filtered query returned ${gigs.size} gigs")
                
                gigs.forEach { gig ->
                    println("Debug: SearchViewModel - Gig: ${gig.title} | Category: ${gig.category} | Status: ${gig.status} | Active: ${gig.isActive}")
                }
                
                // Use filtered gigs if available, otherwise use simple gigs
                val finalGigs = if (gigs.isNotEmpty()) gigs else simpleGigs
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    allGigs = finalGigs
                )
                
                println("Debug: SearchViewModel - Final gigs count: ${finalGigs.size}")
            } catch (e: Exception) {
                println("Debug: SearchViewModel - Error loading all published gigs: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load gigs: ${e.message}"
                )
            }
        }
    }

    /**
     * Clears category selection and shows all categories
     */
    fun clearCategorySelection() {
        _uiState.value = _uiState.value.copy(
            selectedCategory = null,
            categoryGigs = emptyList(),
            searchResults = emptyList()
        )
        _searchQuery.value = ""
    }

    /**
     * Clears error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Refreshes all data
     */
    fun refresh() {
        loadAllPublishedGigs()
        if (_uiState.value.selectedCategory != null) {
            getGigsByCategory(_uiState.value.selectedCategory!!)
        }
    }
    
    /**
     * Fixes data inconsistencies and refreshes
     */
    fun fixDataAndRefresh() {
        viewModelScope.launch {
            try {
                println("Debug: Running data fix from SearchViewModel")
                gigRepository.fixDataInconsistencies()
                // Wait a bit for the fix to complete
                kotlinx.coroutines.delay(2000)
                // Refresh data
                refresh()
            } catch (e: Exception) {
                println("Debug: Error fixing data: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error fixing data: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Force refresh with simple query for debugging
     */
    fun forceRefreshSimple() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                println("Debug: Force refresh with simple query")
                val allGigs = gigRepository.getAllGigsSimple()
                println("Debug: Simple refresh found ${allGigs.size} gigs")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    allGigs = allGigs,
                    errorMessage = if (allGigs.isEmpty()) "No gigs found in database" else null
                )
            } catch (e: Exception) {
                println("Debug: Error in force refresh: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading gigs: ${e.message}"
                )
            }
        }
    }
}
