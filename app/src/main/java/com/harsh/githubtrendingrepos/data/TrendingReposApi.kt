package com.harsh.githubtrendingrepos.data

import com.harsh.githubtrendingrepos.data.model.RepositoriesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TrendingReposApi {

    companion object {
        const val BASE_URL = "https://api.github.com/"
    }

    @GET("search/repositories?sort=stars")
    suspend fun getTrendingRepositories(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per_page") sizeOfPage: Int
    ) : Response<RepositoriesResponse>
}