package com.harsh.githubtrendingrepos.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GithubRepository @Inject constructor(private val api: TrendingReposApi) {

    suspend fun getTrendingRepos(searchQuery: String, pageNumber: Int, resultPerPage :Int)
        = api.getTrendingRepositories(searchQuery, pageNumber, resultPerPage)

}