package com.lumina.tvaggregator.data.model

enum class MonetizationType {
    FREE,
    ADS,
    FLATRATE,
    RENT,
    BUY,
    UNKNOWN
}

enum class PresentationType {
    SD,
    HD,
    UHD_4K,
    UNKNOWN
}

data class Offer(
    val platformName: String,
    val packageName: String?,
    val monetizationType: MonetizationType,
    val presentationType: PresentationType,
    val webUrl: String?,
    val iconUrl: String?
) {
    fun isFree(): Boolean {
        return monetizationType == MonetizationType.FREE || monetizationType == MonetizationType.ADS
    }

    fun getQualityDisplayName(): String {
        return when (presentationType) {
            PresentationType.SD -> "SD"
            PresentationType.HD -> "HD"
            PresentationType.UHD_4K -> "4K"
            PresentationType.UNKNOWN -> ""
        }
    }

    fun getMonetizationDisplayName(): String {
        return when (monetizationType) {
            MonetizationType.FREE -> "Gratuit"
            MonetizationType.ADS -> "Gratuit avec pub"
            MonetizationType.FLATRATE -> "Abonnement"
            MonetizationType.RENT -> "Location"
            MonetizationType.BUY -> "Achat"
            MonetizationType.UNKNOWN -> ""
        }
    }

    fun getDisplayName(): String {
        return buildString {
            append(platformName)
            val quality = getQualityDisplayName()
            val monetization = getMonetizationDisplayName()

            if (quality.isNotEmpty() || monetization.isNotEmpty()) {
                append(" (")
                if (monetization.isNotEmpty()) {
                    append(monetization)
                    if (quality.isNotEmpty()) {
                        append(" - $quality")
                    }
                } else if (quality.isNotEmpty()) {
                    append(quality)
                }
                append(")")
            }
        }
    }

    companion object {
        fun fromApiData(
            platformName: String?,
            packageName: String?,
            monetizationType: String?,
            presentationType: String?,
            webUrl: String?,
            iconUrl: String?
        ): Offer {
            return Offer(
                platformName = platformName ?: "Unknown",
                packageName = packageName,
                monetizationType = parseMonetizationType(monetizationType),
                presentationType = parsePresentationType(presentationType),
                webUrl = webUrl,
                iconUrl = iconUrl
            )
        }

        private fun parseMonetizationType(type: String?): MonetizationType {
            return when (type?.uppercase()) {
                "FREE" -> MonetizationType.FREE
                "ADS" -> MonetizationType.ADS
                "FLATRATE" -> MonetizationType.FLATRATE
                "RENT" -> MonetizationType.RENT
                "BUY" -> MonetizationType.BUY
                else -> MonetizationType.UNKNOWN
            }
        }

        private fun parsePresentationType(type: String?): PresentationType {
            return when (type?.uppercase()) {
                "SD" -> PresentationType.SD
                "HD" -> PresentationType.HD
                "UHD" -> PresentationType.UHD_4K
                else -> PresentationType.UNKNOWN
            }
        }
    }
}