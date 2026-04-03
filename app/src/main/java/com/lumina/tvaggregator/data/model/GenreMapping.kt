package com.lumina.tvaggregator.data.model

/**
 * Maps JustWatch genre shortNames to human-readable French labels.
 */
object GenreMapping {

    private val genreMap = mapOf(
        "act" to "Action",
        "ani" to "Animation",
        "cmy" to "Comédie",
        "crm" to "Policier",
        "doc" to "Documentaire",
        "drm" to "Drame",
        "eur" to "Européen",
        "fml" to "Famille",
        "fnt" to "Fantastique",
        "hrr" to "Horreur",
        "hst" to "Historique",
        "msc" to "Musique",
        "rly" to "Téléréalité",
        "rma" to "Romance",
        "scf" to "Science-Fiction",
        "spt" to "Sport",
        "trl" to "Thriller",
        "war" to "Guerre",
        "wsn" to "Western"
    )

    fun translate(shortName: String): String {
        return genreMap[shortName.lowercase()] ?: shortName.replaceFirstChar { it.uppercase() }
    }

    fun translateList(shortNames: List<String>): List<String> {
        return shortNames.map { translate(it) }
    }
}
