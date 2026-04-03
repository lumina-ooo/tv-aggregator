package com.lumina.tvaggregator.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.tvaggregator.data.PlatformCategory
import com.lumina.tvaggregator.data.PlatformRepository
import com.lumina.tvaggregator.data.SearchFilter
import com.lumina.tvaggregator.data.StreamingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PlatformRepository(application.applicationContext)

    private val _searchFilter = MutableStateFlow(SearchFilter())
    val searchFilter: StateFlow<SearchFilter> = _searchFilter.asStateFlow()

    private val _filteredPlatforms = MutableStateFlow<List<StreamingPlatform>>(emptyList())
    val filteredPlatforms: StateFlow<List<StreamingPlatform>> = _filteredPlatforms.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Combine repository platforms with search filter to produce filtered results
        viewModelScope.launch {
            combine(
                repository.platforms,
                _searchFilter
            ) { platforms, filter ->
                repository.searchPlatforms(filter)
            }.collect { filteredList ->
                _filteredPlatforms.value = filteredList
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchFilter.value = _searchFilter.value.copy(query = query)
    }

    fun updateCategoryFilter(category: PlatformCategory?) {
        _searchFilter.value = _searchFilter.value.copy(category = category)
    }

    fun updateInstalledFilter(installedOnly: Boolean) {
        _searchFilter.value = _searchFilter.value.copy(installedOnly = installedOnly)
    }

    fun clearFilters() {
        _searchFilter.value = SearchFilter()
    }

    fun refreshPlatforms() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.refreshInstallationStatus()
            } catch (e: Exception) {
                _errorMessage.value = "Erreur lors de la mise à jour: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun openPlatform(platform: StreamingPlatform, context: Context) {
        viewModelScope.launch {
            try {
                if (platform.isInstalled) {
                    // Try to launch the app directly
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(platform.packageName)
                    if (launchIntent != null) {
                        context.startActivity(launchIntent)
                    } else {
                        // Fallback to Play Store if app is installed but can't be launched
                        openInPlayStore(platform.packageName, context)
                    }
                } else {
                    // Open in Play Store for installation
                    openInPlayStore(platform.packageName, context)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Impossible d'ouvrir ${platform.name}: ${e.message}"
            }
        }
    }

    private fun openInPlayStore(packageName: String, context: Context) {
        try {
            // Try to open in Play Store app
            val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(playStoreIntent)
        } catch (e: Exception) {
            try {
                // Fallback to web browser
                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            } catch (webException: Exception) {
                _errorMessage.value = "Impossible d'ouvrir le Play Store"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun getPlatformsByCategory(): Map<PlatformCategory, List<StreamingPlatform>> {
        return _filteredPlatforms.value.groupBy { it.category }
    }

    fun getInstalledPlatforms(): List<StreamingPlatform> {
        return _filteredPlatforms.value.filter { it.isInstalled }
    }

    fun getUninstalledPlatforms(): List<StreamingPlatform> {
        return _filteredPlatforms.value.filter { !it.isInstalled }
    }
}