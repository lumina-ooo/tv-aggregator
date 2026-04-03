package com.lumina.tvaggregator.data.api

import com.lumina.tvaggregator.data.model.Content
import com.lumina.tvaggregator.data.model.Offer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class JustWatchRepository {

    private val api: JustWatchApi

    init {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://apis.justwatch.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(JustWatchApi::class.java)
    }

    suspend fun searchTitles(query: String): Result<List<Content>> = withContext(Dispatchers.IO) {
        try {
            val request = GraphQLRequest(
                operationName = "GetSearchTitles",
                query = JustWatchQueries.SEARCH_TITLES,
                variables = mapOf(
                    "first" to 20,
                    "searchTitlesFilter" to mapOf("searchQuery" to query),
                    "language" to "fr",
                    "country" to "BE",
                    "formatPoster" to "JPG",
                    "formatOfferIcon" to "PNG",
                    "profile" to "S718",
                    "filter" to mapOf("bestOnly" to true)
                )
            )

            executeQuery(request)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPopularTitles(): Result<List<Content>> = withContext(Dispatchers.IO) {
        try {
            val request = GraphQLRequest(
                operationName = "GetPopularTitles",
                query = JustWatchQueries.POPULAR_TITLES,
                variables = mapOf(
                    "first" to 40,
                    "popularTitlesFilter" to emptyMap<String, Any>(),
                    "language" to "fr",
                    "country" to "BE",
                    "formatPoster" to "JPG",
                    "formatOfferIcon" to "PNG",
                    "profile" to "S718",
                    "filter" to mapOf("bestOnly" to true)
                )
            )

            executeQuery(request)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getContentByPlatform(platformShortName: String): Result<List<Content>> = withContext(Dispatchers.IO) {
        try {
            val request = GraphQLRequest(
                operationName = "GetPopularTitles",
                query = JustWatchQueries.POPULAR_TITLES,
                variables = mapOf(
                    "first" to 40,
                    "popularTitlesFilter" to mapOf("packages" to listOf(platformShortName)),
                    "language" to "fr",
                    "country" to "BE",
                    "formatPoster" to "JPG",
                    "formatOfferIcon" to "PNG",
                    "profile" to "S718",
                    "filter" to mapOf("bestOnly" to true)
                )
            )

            executeQuery(request)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun executeQuery(request: GraphQLRequest): Result<List<Content>> {
        val response = api.query(request)

        if (!response.isSuccessful) {
            return Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
        }

        val body = response.body()
        if (body?.errors != null && body.errors.isNotEmpty()) {
            return Result.failure(Exception("GraphQL: ${body.errors.first().message}"))
        }

        val content = body?.data?.popularTitles?.edges?.mapNotNull { edge ->
            edge?.node?.let { mapTitleNodeToContent(it) }
        } ?: emptyList()

        return Result.success(content)
    }

    private fun mapTitleNodeToContent(node: TitleNode): Content? {
        val contentData = node.content ?: return null
        val title = contentData.title ?: return null

        val offers = node.offers?.mapNotNull { offerData ->
            val packageData = offerData.packageInfo
            if (packageData == null) return@mapNotNull null

            Offer.fromApiData(
                platformName = packageData.clearName ?: packageData.shortName ?: "Unknown",
                packageName = packageData.technicalName,
                monetizationType = offerData.monetizationType,
                presentationType = offerData.presentationType,
                webUrl = offerData.standardWebUrl,
                iconUrl = packageData.icon
            )
        } ?: emptyList()

        val genres = contentData.genres?.mapNotNull { it.shortName } ?: emptyList()
        val imdbScore = contentData.scoring?.imdbScore

        return Content(
            id = node.id ?: "",
            objectId = node.objectId ?: 0,
            title = title,
            originalReleaseYear = contentData.originalReleaseYear,
            description = contentData.shortDescription,
            posterUrl = contentData.posterUrl,
            genres = genres,
            imdbScore = imdbScore,
            offers = offers,
            fullPath = contentData.fullPath
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: JustWatchRepository? = null

        fun getInstance(): JustWatchRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: JustWatchRepository().also { INSTANCE = it }
            }
        }
    }
}
