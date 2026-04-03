package com.lumina.tvaggregator.data

data class StreamingPlatform(
    val id: String,
    val name: String,
    val description: String,
    val packageName: String,
    val logoResourceId: Int,
    val category: PlatformCategory,
    val country: String,
    val isInstalled: Boolean = false
)

enum class PlatformCategory {
    NATIONAL_TV,    // TF1+, France.tv, M6+
    REGIONAL_TV,    // RTL TVI+, Auvio/RTBF
    CULTURAL,       // Arte
    INTERNATIONAL,  // Pluto TV, Rakuten TV
    LIVE_TV        // Molotov
}

data class SearchFilter(
    val query: String = "",
    val category: PlatformCategory? = null,
    val country: String? = null,
    val installedOnly: Boolean = false
)