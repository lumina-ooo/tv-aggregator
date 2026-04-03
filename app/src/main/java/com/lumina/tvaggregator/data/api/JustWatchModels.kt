package com.lumina.tvaggregator.data.api

import com.google.gson.annotations.SerializedName

// GraphQL Request models
data class GraphQLRequest<T>(
    val query: String,
    val variables: T
)

// Search request models
data class SearchTitlesInput(
    val searchQuery: String,
    val country: String,
    val language: String,
    val count: Int,
    val monetizationTypes: List<String>
)

data class SearchTitlesVariables(
    val searchTitlesInput: SearchTitlesInput
)

// Popular titles request models
data class PopularTitlesInput(
    val country: String,
    val language: String,
    val count: Int,
    val monetizationTypes: List<String>
)

data class PopularTitlesVariables(
    val popularTitlesInput: PopularTitlesInput
)

// Response models
data class GraphQLResponse<T>(
    val data: T?,
    val errors: List<GraphQLError>?
)

data class GraphQLError(
    val message: String,
    val locations: List<ErrorLocation>?
)

data class ErrorLocation(
    val line: Int,
    val column: Int
)

// Search response models
data class SearchTitlesResponse(
    val searchTitles: SearchTitlesConnection
)

data class PopularTitlesResponse(
    val popularTitles: PopularTitlesConnection
)

data class SearchTitlesConnection(
    val edges: List<TitleEdge>
)

data class PopularTitlesConnection(
    val edges: List<TitleEdge>
)

data class TitleEdge(
    val node: TitleNode
)

data class TitleNode(
    val id: String,
    val objectId: Int,
    val objectType: String,
    val content: ContentData?,
    val offers: List<OfferData>?
)

data class ContentData(
    val title: String?,
    val originalReleaseYear: Int?,
    val shortDescription: String?,
    val fullPath: String?,
    val posterUrl: String?,
    val genres: List<GenreData>?,
    val scoring: List<ScoringData>?
)

data class GenreData(
    val shortName: String?,
    val translation: String?
)

data class ScoringData(
    val providerType: String?,
    val value: Double?
)

data class OfferData(
    val monetizationType: String?,
    val presentationType: String?,
    @SerializedName("standardWebURL")
    val standardWebUrl: String?,
    val package: PackageData?
)

data class PackageData(
    val packageId: Int?,
    val clearName: String?,
    val shortName: String?,
    val icon: String?,
    val technicalName: String?
)

// Query constants
object JustWatchQueries {
    const val SEARCH_TITLES = """
        query SearchTitles(${'$'}searchTitlesInput: SearchTitlesInput!) {
          searchTitles(input: ${'$'}searchTitlesInput) {
            edges {
              node {
                id
                objectId
                objectType
                content(country: "BE", language: "fr") {
                  title
                  originalReleaseYear
                  shortDescription
                  fullPath
                  posterUrl
                  genres {
                    shortName
                    translation
                  }
                  scoring {
                    providerType
                    value
                  }
                }
                offers(country: "BE", platform: "ANDROID_TV") {
                  monetizationType
                  presentationType
                  standardWebURL
                  package {
                    packageId
                    clearName
                    shortName
                    icon
                    technicalName
                  }
                }
              }
            }
          }
        }
    """

    const val POPULAR_TITLES = """
        query PopularTitles(${'$'}popularTitlesInput: PopularTitlesInput!) {
          popularTitles(input: ${'$'}popularTitlesInput) {
            edges {
              node {
                id
                objectId
                objectType
                content(country: "BE", language: "fr") {
                  title
                  originalReleaseYear
                  shortDescription
                  fullPath
                  posterUrl
                  genres {
                    shortName
                    translation
                  }
                  scoring {
                    providerType
                    value
                  }
                }
                offers(country: "BE", platform: "ANDROID_TV") {
                  monetizationType
                  presentationType
                  standardWebURL
                  package {
                    packageId
                    clearName
                    shortName
                    icon
                    technicalName
                  }
                }
              }
            }
          }
        }
    """
}