package com.lumina.tvaggregator.data.api

import com.google.gson.annotations.SerializedName

// Generic GraphQL request - both search and popular use the same structure
data class GraphQLRequest(
    val operationName: String,
    val query: String,
    val variables: Map<String, Any?>
)

// Response models
data class GraphQLResponse(
    val data: ResponseData?,
    val errors: List<GraphQLError>?
)

data class GraphQLError(
    val message: String
)

data class ResponseData(
    val popularTitles: TitleConnection?
)

data class TitleConnection(
    val edges: List<TitleEdge>?
)

data class TitleEdge(
    val node: TitleNode?
)

data class TitleNode(
    val id: String?,
    val objectId: Int?,
    val objectType: String?,
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
    val scoring: ScoringData?
)

data class GenreData(
    val shortName: String?
)

data class ScoringData(
    val imdbScore: Double?,
    val tmdbScore: Double?,
    val jwRating: Double?
)

data class OfferData(
    val monetizationType: String?,
    val presentationType: String?,
    @SerializedName("standardWebURL")
    val standardWebUrl: String?,
    @SerializedName("package")
    val packageInfo: PackageData?
)

data class PackageData(
    val packageId: Int?,
    val clearName: String?,
    val shortName: String?,
    val icon: String?,
    val technicalName: String?
)

// Real JustWatch GraphQL queries (verified against live API)
object JustWatchQueries {

    // Both search and popular use the same "popularTitles" endpoint.
    // Search adds a searchQuery to the TitleFilter.
    const val SEARCH_TITLES = """
query GetSearchTitles(
  ${'$'}searchTitlesFilter: TitleFilter!,
  ${'$'}country: Country!,
  ${'$'}language: Language!,
  ${'$'}first: Int!,
  ${'$'}formatPoster: ImageFormat,
  ${'$'}formatOfferIcon: ImageFormat,
  ${'$'}profile: PosterProfile,
  ${'$'}filter: OfferFilter!,
  ${'$'}offset: Int = 0
) {
  popularTitles(
    country: ${'$'}country
    filter: ${'$'}searchTitlesFilter
    first: ${'$'}first
    sortBy: POPULAR
    sortRandomSeed: 0
    offset: ${'$'}offset
  ) {
    edges {
      node {
        id
        objectId
        objectType
        content(country: ${'$'}country, language: ${'$'}language) {
          title
          originalReleaseYear
          shortDescription
          fullPath
          posterUrl(profile: ${'$'}profile, format: ${'$'}formatPoster)
          genres { shortName }
          scoring { imdbScore tmdbScore jwRating }
        }
        offers(country: ${'$'}country, platform: WEB, filter: ${'$'}filter) {
          monetizationType
          presentationType
          standardWebURL
          package {
            packageId
            clearName
            shortName
            technicalName
            icon(profile: S100, format: ${'$'}formatOfferIcon)
          }
        }
      }
    }
  }
}
"""

    const val POPULAR_TITLES = """
query GetPopularTitles(
  ${'$'}popularTitlesFilter: TitleFilter,
  ${'$'}country: Country!,
  ${'$'}language: Language!,
  ${'$'}first: Int! = 40,
  ${'$'}formatPoster: ImageFormat,
  ${'$'}formatOfferIcon: ImageFormat,
  ${'$'}profile: PosterProfile,
  ${'$'}filter: OfferFilter!,
  ${'$'}offset: Int = 0
) {
  popularTitles(
    country: ${'$'}country
    filter: ${'$'}popularTitlesFilter
    first: ${'$'}first
    sortBy: POPULAR
    sortRandomSeed: 0
    offset: ${'$'}offset
  ) {
    edges {
      node {
        id
        objectId
        objectType
        content(country: ${'$'}country, language: ${'$'}language) {
          title
          originalReleaseYear
          shortDescription
          fullPath
          posterUrl(profile: ${'$'}profile, format: ${'$'}formatPoster)
          genres { shortName }
          scoring { imdbScore tmdbScore jwRating }
        }
        offers(country: ${'$'}country, platform: WEB, filter: ${'$'}filter) {
          monetizationType
          presentationType
          standardWebURL
          package {
            packageId
            clearName
            shortName
            technicalName
            icon(profile: S100, format: ${'$'}formatOfferIcon)
          }
        }
      }
    }
  }
}
"""
}
