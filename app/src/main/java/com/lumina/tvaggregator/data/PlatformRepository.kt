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
            packageName = "fr.tf1.mytf1",
            logoResourceId = R.drawable.logo_tf1plus,
            category = PlatformCategory.NATIONAL_TV,
            country = "France"
        ),
        StreamingPlatform(
            id = "arte",
            name = "Arte",
            description = "La chaîne culturelle franco-allemande — Documentaires, films d'auteur et arts",
            packageName = "tv.arte.plus7",
            logoResourceId = R.drawable.logo_arte,
            category = PlatformCategory.CULTURAL,
            country = "France"
        ),
        StreamingPlatform(
            id = "amazonprime",
            name = "Amazon Prime Video",
            description = "Films, séries et productions Amazon Originals",
            packageName = "com.amazon.amazonvideo.livingroom",
            logoResourceId = R.drawable.logo_amazonprime,
            category = PlatformCategory.INTERNATIONAL,
            country = "Belgique"
        ),
        StreamingPlatform(
            id = "francetvchannel",
            name = "France TV (Amazon Channel)",
            description = "Chaînes France Télévisions accessibles via Amazon Prime",
            packageName = null,
            logoResourceId = R.drawable.logo_francetvch,
            category = PlatformCategory.NATIONAL_TV,
            country = "France"
        ),
        StreamingPlatform(
            id = "tfoumax",
            name = "TFOU Max (Amazon Channel)",
            description = "Contenu jeunesse et enfants — Dessins animés et programmes familiaux (via Amazon Prime)",
            packageName = null,
            logoResourceId = R.drawable.logo_tfoumax,
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
            platform.copy(isInstalled = platform.packageName?.let { isPackageInstalled(packageManager, it) } ?: false)
        }
        _platforms.value = updatedPlatforms
    }

    private fun isPackageInstalled(packageManager: PackageManager, packageName: String?): Boolean {
        if (packageName == null) return false
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