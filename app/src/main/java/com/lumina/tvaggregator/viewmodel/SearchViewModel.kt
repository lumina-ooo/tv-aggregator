package com.lumina.tvaggregator.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.tvaggregator.data.api.JustWatchRepository
import com.lumina.tvaggregator.data.model.Content
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val justWatchRepository = JustWatchRepository.getInstance()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Content>>(emptyList())
    val searchResults: StateFlow<List<Content>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Set up search with debouncing
        setupSearchFlow()
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchFlow() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // Wait 300ms after user stops typing
                .filter { it.trim().length >= 2 } // Only search if query has at least 2 characters
                .distinctUntilChanged() // Only search if query actually changed
                .collect { query ->
                    performSearch(query.trim())
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query

        // Clear results immediately if query is empty or too short
        if (query.trim().length < 2) {
            _searchResults.value = emptyList()
            _isLoading.value = false
        }
    }

    private suspend fun performSearch(query: String) {
        _isLoading.value = true
        _errorMessage.value = null

        try {
            val result = justWatchRepository.searchTitles(query)
            if (result.isSuccess) {
                val content = result.getOrNull() ?: emptyList()
                _searchResults.value = content
            } else {
                _errorMessage.value = "Erreur lors de la recherche: ${result.exceptionOrNull()?.message}"
                _searchResults.value = emptyList()
            }
        } catch (e: Exception) {
            _errorMessage.value = "Erreur lors de la recherche: ${e.message}"
            _searchResults.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _isLoading.value = false
        _errorMessage.value = null
    }

    fun retrySearch() {
        val currentQuery = _searchQuery.value.trim()
        if (currentQuery.length >= 2) {
            viewModelScope.launch {
                performSearch(currentQuery)
            }
        }
    }

    fun openContent(content: Content, context: Context) {
        viewModelScope.launch {
            try {
                val freeOffer = content.getFreeOffers().firstOrNull()
                if (freeOffer?.webUrl != null) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(freeOffer.webUrl))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } else {
                    _errorMessage.value = "Aucun lien disponible pour ${content.title}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Impossible d'ouvrir ${content.title}: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun getCurrentResults(): List<Content> {
        return _searchResults.value
    }

    fun hasResults(): Boolean {
        return _searchResults.value.isNotEmpty()
    }

    fun getResultCount(): Int {
        return _searchResults.value.size
    }
}