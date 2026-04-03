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
                val result = justWatchRepository.getContentByPlatform(platform.shortName)
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
                id = "tf1",
                name = "TF1+",
                description = "Télévision française - Replay et directs",
                shortName = "tfi",
                packageName = "com.tf1.tf1plus"
            ),
            SupportedPlatform(
                id = "rtl",
                name = "RTL TVI+",
                description = "Télévision belge - Contenu RTL Belgique",
                shortName = "rtb",
                packageName = "be.rtl.rtlplay"
            ),
            SupportedPlatform(
                id = "auvio",
                name = "Auvio (RTBF)",
                description = "Service public belge - Documentaires et séries",
                shortName = "auv",
                packageName = "be.rtbf.auvio"
            ),
            SupportedPlatform(
                id = "francetv",
                name = "France.tv",
                description = "Service public français - Large catalogue",
                shortName = "ftv",
                packageName = "fr.francetv.francetv"
            ),
            SupportedPlatform(
                id = "m6plus",
                name = "M6+ / 6play",
                description = "Groupe M6 - Divertissement français",
                shortName = "6pl",
                packageName = "fr.m6.m6plus"
            ),
            SupportedPlatform(
                id = "arte",
                name = "Arte",
                description = "Franco-allemande - Culture et documentaires",
                shortName = "art",
                packageName = "tv.arte.plus7"
            ),
            SupportedPlatform(
                id = "pluto",
                name = "Pluto TV",
                description = "Chaînes TV gratuites en streaming",
                shortName = "ptv",
                packageName = "tv.pluto.android"
            ),
            SupportedPlatform(
                id = "rakuten",
                name = "Rakuten TV",
                description = "Films et séries gratuits avec publicités",
                shortName = "rak",
                packageName = "com.rakuten.tv.app"
            ),
            SupportedPlatform(
                id = "molotov",
                name = "Molotov",
                description = "TV française en direct et replay",
                shortName = "mol",
                packageName = "tv.molotov.app"
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