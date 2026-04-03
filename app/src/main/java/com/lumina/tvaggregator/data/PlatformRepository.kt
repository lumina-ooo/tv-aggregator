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
            id = "tf1plus",
            name = "TF1+",
            description = "La plateforme gratuite de TF1 avec replays et programmes exclusifs",
            packageName = "com.tf1.mytf1",
            logoResourceId = R.drawable.logo_tf1plus,
            category = PlatformCategory.NATIONAL_TV,
            country = "France"
        ),
        StreamingPlatform(
            id = "rtltviplus",
            name = "RTL TVI+",
            description = "RTL TVI en direct et en replay",
            packageName = "be.rtl.rtlplay",
            logoResourceId = R.drawable.logo_rtltviplus,
            category = PlatformCategory.REGIONAL_TV,
            country = "Belgique"
        ),
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
            id = "francetv",
            name = "France.tv",
            description = "Tous les programmes de France Télévisions en replay",
            packageName = "fr.francetv.pluzz",
            logoResourceId = R.drawable.logo_francetv,
            category = PlatformCategory.NATIONAL_TV,
            country = "France"
        ),
        StreamingPlatform(
            id = "m6plus",
            name = "M6+",
            description = "Les programmes du Groupe M6 en replay et en direct",
            packageName = "fr.m6.m6replay",
            logoResourceId = R.drawable.logo_m6plus,
            category = PlatformCategory.NATIONAL_TV,
            country = "France"
        ),
        StreamingPlatform(
            id = "arte",
            name = "ARTE",
            description = "Documentaires, films, séries et concerts",
            packageName = "tv.arte.plus7",
            logoResourceId = R.drawable.logo_arte,
            category = PlatformCategory.CULTURAL,
            country = "Franco-Allemande"
        ),
        StreamingPlatform(
            id = "plutotv",
            name = "Pluto TV",
            description = "TV gratuite avec plus de 100 chaînes",
            packageName = "tv.pluto.android",
            logoResourceId = R.drawable.logo_plutotv,
            category = PlatformCategory.INTERNATIONAL,
            country = "International"
        ),
        StreamingPlatform(
            id = "rakutentv",
            name = "Rakuten TV",
            description = "Films et séries gratuits avec publicité",
            packageName = "tv.wuaki.apptv",
            logoResourceId = R.drawable.logo_rakutentv,
            category = PlatformCategory.INTERNATIONAL,
            country = "International"
        ),
        StreamingPlatform(
            id = "molotov",
            name = "Molotov",
            description = "Télévision française en direct et en replay",
            packageName = "tv.molotov.app",
            logoResourceId = R.drawable.logo_molotov,
            category = PlatformCategory.LIVE_TV,
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