package com.lumina.tvaggregator.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.tvaggregator.data.api.JustWatchRepository
import com.lumina.tvaggregator.data.model.Content
import com.lumina.tvaggregator.data.model.GenreMapping
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContentByGenre(
    val genre: String,
    val content: List<Content>
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val justWatchRepository = JustWatchRepository.getInstance()

    private val _contentByGenre = MutableStateFlow<List<ContentByGenre>>(emptyList())
    val contentByGenre: StateFlow<List<ContentByGenre>> = _contentByGenre.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadPopularContent()
    }

    private fun loadPopularContent() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = justWatchRepository.getPopularTitles()
                if (result.isSuccess) {
                    val content = result.getOrNull() ?: emptyList()
                    organizeContentByGenre(content)
                } else {
                    _errorMessage.value = "Erreur lors du chargement du contenu: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur lors du chargement du contenu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun organizeContentByGenre(content: List<Content>) {
        val contentByGenreMap = mutableMapOf<String, MutableList<Content>>()

        content.forEach { item ->
            item.genres.forEach { genre ->
                // Translate genre shortName to French label
                val translatedGenre = GenreMapping.translate(genre)
                contentByGenreMap.getOrPut(translatedGenre) { mutableListOf() }.add(item)
            }
        }

        // Sort genres by content count and take top genres
        val sortedGenres = contentByGenreMap.entries
            .sortedByDescending { it.value.size }
            .take(8) // Limit to 8 genres for better UI
            .map { (genre, contentList) ->
                ContentByGenre(
                    genre = genre,
                    content = contentList.sortedByDescending { it.imdbScore ?: 0.0 }.take(20)
                )
            }

        _contentByGenre.value = sortedGenres
    }

    fun refreshContent() {
        loadPopularContent()
    }

    fun openContent(content: Content, context: Context) {
        viewModelScope.launch {
            try {
                // Try free offers first, then any offer with a URL
                val offer = content.getFreeOffers().firstOrNull()
                    ?: content.offers.firstOrNull { it.webUrl != null }
                if (offer?.webUrl != null) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(offer.webUrl))
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

    fun getAllContent(): List<Content> {
        return _contentByGenre.value.flatMap { it.content }.distinctBy { it.id }
    }

    fun getContentByGenre(genre: String): List<Content> {
        return _contentByGenre.value.find { it.genre == genre }?.content ?: emptyList()
    }
}