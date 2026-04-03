package com.lumina.tvaggregator.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface JustWatchApi {

    @Headers("Content-Type: application/json")
    @POST("graphql")
    suspend fun searchTitles(
        @Body request: GraphQLRequest<SearchTitlesVariables>
    ): Response<GraphQLResponse<SearchTitlesResponse>>

    @Headers("Content-Type: application/json")
    @POST("graphql")
    suspend fun getPopularTitles(
        @Body request: GraphQLRequest<PopularTitlesVariables>
    ): Response<GraphQLResponse<PopularTitlesResponse>>
}