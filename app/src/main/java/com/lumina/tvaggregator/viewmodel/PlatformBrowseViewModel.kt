package com.lumina.tvaggregator.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.tvaggregator.data.api.JustWatchRepository
import com.lumina.tvaggregator.data.model.Content
import com.lumina.tvaggregator.ui.screens.SupportedPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlatformBrowseViewModel(application: Application) : AndroidViewModel(application) {

    private val justWatchRepository = JustWatchRepository.getInstance()

    private val _platforms = MutableStateFlow(getSupportedPlatforms())
    val platforms: StateFlow<List<SupportedPlatform>> = _platforms.asStateFlow()

    private val _selectedPlatform = MutableStateFlow<SupportedPlatform?>(null)
    val selectedPlatform: StateFlow<SupportedPlatform?> = _selectedPlatform.asStateFlow()

    private val _platformContent = MutableStateFlow<List<Content>>(emptyList())
    val platformContent: StateFlow<List<Content>> = _platformContent.asStateFlow()

    private val _isLoadingContent = MutableStateFlow(false)
    val isLoadingContent: StateFlow<Boolean> = _isLoadingContent.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun selectPlatform(platform: SupportedPlatform) {
        _selectedPlatform.value = platform
        loadPlatformContent(platform)
    }

    fun clearSelectedPlatform() {
        _selectedPlatform.value = null
        _platformContent.value = emptyList()
        _isLoadingContent.value = false
    }

    private fun loadPlatformContent(platform: SupportedPlatform) {
        viewModelScope.launch {
            _isLoadingContent.value = true
            _errorMessage.value = null

            try {
                val result = justWatchRepository.getContentByPlatform(platform.shortName, platform.country)
                if (result.isSuccess) {
                    val content = result.getOrNull() ?: emptyList()
                    _platformContent.value = content
                } else {
                    _errorMessage.value = "Erreur lors du chargement: ${result.exceptionOrNull()?.message}"
                    _platformContent.value = emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur lors du chargement: ${e.message}"
                _platformContent.value = emptyList()
            } finally {
                _isLoadingContent.value = false
            }
        }
    }

    fun retryLoadPlatformContent() {
        _selectedPlatform.value?.let { platform ->
            loadPlatformContent(platform)
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

    private fun getSupportedPlatforms(): List<SupportedPlatform> {
        return listOf(
            SupportedPlatform(
                id = "auvio",
                name = "Auvio (RTBF)",
                description = "Service public belge — Documentaires, séries et films",
                shortName = "rtb",
                country = "BE",
                packageName = "be.rtbf.auvio"
            ),
            SupportedPlatform(
                id = "rtl",
                name = "RTL Play",
                description = "RTL Belgique — Séries, divertissement et replay",
                shortName = "rtl",
                country = "BE",
                packageName = "be.rtl.rtlplay"
            ),
            SupportedPlatform(
                id = "tf1",
                name = "TF1+",
                description = "TF1 France — Replay, séries et programmes exclusifs",
                shortName = "tf1",
                country = "FR",
                packageName = "com.tf1.mytf1"
            )
        )
    }

    fun getCurrentPlatformContentCount(): Int {
        return _platformContent.value.size
    }

    fun hasContentForCurrentPlatform(): Boolean {
        return _platformContent.value.isNotEmpty()
    }

    fun getAllAvailablePlatforms(): List<SupportedPlatform> {
        return _platforms.value
    }
}