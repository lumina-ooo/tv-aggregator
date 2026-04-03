package com.lumina.tvaggregator.data.api

import com.lumina.tvaggregator.data.model.Content
import com.lumina.tvaggregator.data.model.Offer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class JustWatchRepository {

    private val api: JustWatchApi

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
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
                query = JustWatchQueries.SEARCH_TITLES,
                variables = SearchTitlesVariables(
                    SearchTitlesInput(
                        searchQuery = query,
                        country = "BE",
                        language = "fr",
                        count = 20,
                        monetizationTypes = listOf("FREE", "ADS", "FLATRATE_AND_BUY")
                    )
                )
            )

            val response = api.searchTitles(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.errors != null && body.errors.isNotEmpty()) {
                    Result.failure(Exception("GraphQL Error: ${body.errors.first().message}"))
                } else {
                    val content = body?.data?.searchTitles?.edges?.mapNotNull { edge ->
                        mapTitleNodeToContent(edge.node)
                    } ?: emptyList()
                    Result.success(content)
                }
            } else {
                Result.failure(Exception("HTTP Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPopularTitles(): Result<List<Content>> = withContext(Dispatchers.IO) {
        try {
            val request = GraphQLRequest(
                query = JustWatchQueries.POPULAR_TITLES,
                variables = PopularTitlesVariables(
                    PopularTitlesInput(
                        country = "BE",
                        language = "fr",
                        count = 40,
                        monetizationTypes = listOf("FREE", "ADS")
                    )
                )
            )

            val response = api.getPopularTitles(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.errors != null && body.errors.isNotEmpty()) {
                    Result.failure(Exception("GraphQL Error: ${body.errors.first().message}"))
                } else {
                    val content = body?.data?.popularTitles?.edges?.mapNotNull { edge ->
                        mapTitleNodeToContent(edge.node)
                    } ?: emptyList()
                    Result.success(content)
                }
            } else {
                Result.failure(Exception("HTTP Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getContentByPlatform(platformShortName: String): Result<List<Content>> = withContext(Dispatchers.IO) {
        try {
            // We'll use the popular titles query but filter by platform in the result
            // JustWatch doesn't have a dedicated by-platform query in their public GraphQL
            val popularResult = getPopularTitles()

            if (popularResult.isSuccess) {
                val allContent = popularResult.getOrNull() ?: emptyList()
                val platformContent = allContent.filter { content ->
                    content.offers.any { offer ->
                        offer.packageName?.lowercase()?.contains(platformShortName.lowercase()) == true ||
                        offer.platformName.lowercase().contains(platformShortName.lowercase())
                    }
                }
                Result.success(platformContent)
            } else {
                popularResult
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapTitleNodeToContent(node: TitleNode): Content? {
        val contentData = node.content ?: return null
        val title = contentData.title ?: return null

        val offers = node.offers?.mapNotNull { offerData ->
            val packageData = offerData.packageInfo
            Offer.fromApiData(
                platformName = packageData?.clearName ?: packageData?.shortName ?: "Unknown",
                packageName = packageData?.technicalName,
                monetizationType = offerData.monetizationType,
                presentationType = offerData.presentationType,
                webUrl = offerData.standardWebUrl,
                iconUrl = packageData?.icon
            )
        }?.filter {
            // Only include free content and filter for Belgian/French platforms
            it.isFree() && isSupportedPlatform(it.platformName, it.packageName)
        } ?: emptyList()

        // Only return content that has at least one free offer
        if (offers.isEmpty()) return null

        val genres = contentData.genres?.mapNotNull { it.translation ?: it.shortName } ?: emptyList()
        val imdbScore = contentData.scoring?.find { it.providerType == "imdb:score" }?.value

        return Content(
            id = node.id,
            objectId = node.objectId,
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

    private fun isSupportedPlatform(platformName: String, packageName: String?): Boolean {
        val supportedPlatforms = listOf(
            "tfi", "rtb", "auv", "ftv", "6pl", "art", "ptv", "rak", "mol",
            "tf1", "rtl", "auvio", "france", "m6", "arte", "pluto", "rakuten", "molotov"
        )

        val lowerPlatformName = platformName.lowercase()
        val lowerPackageName = packageName?.lowercase() ?: ""

        return supportedPlatforms.any { platform ->
            lowerPlatformName.contains(platform) || lowerPackageName.contains(platform)
        }
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