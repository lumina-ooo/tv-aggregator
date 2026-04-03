package com.lumina.tvaggregator.data.model

data class Content(
    val id: String,
    val objectId: Int,
    val title: String,
    val originalReleaseYear: Int?,
    val description: String?,
    val posterUrl: String?,
    val genres: List<String>,
    val imdbScore: Double?,
    val offers: List<Offer>,
    val fullPath: String?
) {
    fun getPosterImageUrl(): String? {
        // posterUrl already contains full URL from repository (prefixed with https://images.justwatch.com)
        return posterUrl
    }

    fun getGenresText(): String {
        return genres.joinToString(", ")
    }

    fun hasValidPoster(): Boolean {
        return !posterUrl.isNullOrBlank()
    }

    fun getDisplayTitle(): String {
        return if (originalReleaseYear != null) {
            "$title ($originalReleaseYear)"
        } else {
            title
        }
    }

    fun hasFreeOffers(): Boolean {
        return offers.any { it.isFree() }
    }

    fun getFreeOffers(): List<Offer> {
        return offers.filter { it.isFree() }
    }
}