package com.lumina.tvaggregator.data

import android.content.Context
import android.content.pm.PackageManager
import com.lumina.tvaggregator.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlatformRepository(private val context: Context) {

    private val _platforms = MutableStateFlow<List<StreamingPlatform>>(emptyList())
    val platforms: StateFlow<List<StreamingPlatform>> = _platforms.asStateFlow()

    private val defaultPlatforms = listOf(
        StreamingPlatform(
            id = "auvio",
            name = "Auvio",
            description = "La plateforme numérique de la RTBF",
            packageName = "be.rtbf.auvio",
            logoResourceId = R.drawable.logo_auvio,
            category = PlatformCategory.REGIONAL_TV,
            country = "Belgique"
        ),
        StreamingPlatform(
            id = "rtltviplus",
            name = "RTL Play",
            description = "RTL TVI en direct et en replay",
            packageName = "be.rtl.rtlplay",
            logoResourceId = R.drawable.logo_rtltviplus,
            category = PlatformCategory.REGIONAL_TV,
            country = "Belgique"
        ),
        StreamingPlatform(
            id = "tf1plus",
            name = "TF1+",
            description = "La plateforme gratuite de TF1 avec replays et programmes exclusifs",
            packageName = "com.tf1.mytf1",
            logoResourceId = R.drawable.logo_tf1plus,
            category = PlatformCategory.NATIONAL_TV,
            country = "France"
        )
    )

    init {
        updatePlatformsWithInstallationStatus()
    }

    private fun updatePlatformsWithInstallationStatus() {
        val packageManager = context.packageManager
        val updatedPlatforms = defaultPlatforms.map { platform ->
            platform.copy(isInstalled = isPackageInstalled(packageManager, platform.packageName))
        }
        _platforms.value = updatedPlatforms
    }

    private fun isPackageInstalled(packageManager: PackageManager, packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun searchPlatforms(filter: SearchFilter): List<StreamingPlatform> {
        return platforms.value.filter { platform ->
            val matchesQuery = filter.query.isEmpty() ||
                platform.name.contains(filter.query, ignoreCase = true) ||
                platform.description.contains(filter.query, ignoreCase = true)

            val matchesCategory = filter.category == null || platform.category == filter.category
            val matchesCountry = filter.country == null || platform.country == filter.country
            val matchesInstalled = !filter.installedOnly || platform.isInstalled

            matchesQuery && matchesCategory && matchesCountry && matchesInstalled
        }
    }

    fun refreshInstallationStatus() {
        updatePlatformsWithInstallationStatus()
    }
}