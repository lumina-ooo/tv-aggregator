package com.lumina.tvaggregator.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.tvaggregator.data.api.JustWatchRepository
import com.lumina.tvaggregator.data.model.Content
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KidsViewModel(application: Application) : AndroidViewModel(application) {

    private val justWatchRepository = JustWatchRepository.getInstance()

    private val _kidsContent = MutableStateFlow<List<Content>>(emptyList())
    val kidsContent: StateFlow<List<Content>> = _kidsContent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadKidsContent()
    }

    fun loadKidsContent() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Chargement en parallèle depuis BE et FR pour maximiser le contenu
                val beDeferred = async { justWatchRepository.getKidsContent("BE") }
                val frDeferred = async { justWatchRepository.getKidsContent("FR") }

                val beResult = beDeferred.await()
                val frResult = frDeferred.await()

                val beContent = beResult.getOrElse { emptyList() }
                val frContent = frResult.getOrElse { emptyList() }

                // Fusion et déduplification par id
                val merged = (beContent + frContent)
                    .distinctBy { it.id }
                    .sortedByDescending { it.imdbScore ?: 0.0 }

                if (merged.isEmpty() && beResult.isFailure && frResult.isFailure) {
                    _errorMessage.value = "Impossible de charger le contenu enfant : ${beResult.exceptionOrNull()?.message}"
                }

                _kidsContent.value = merged
            } catch (e: Exception) {
                _errorMessage.value = "Erreur lors du chargement : ${e.message}"
                _kidsContent.value = emptyList()
            } finally {
                _isLoading.value = false
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
                _errorMessage.value = "Impossible d'ouvrir ${content.title} : ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
