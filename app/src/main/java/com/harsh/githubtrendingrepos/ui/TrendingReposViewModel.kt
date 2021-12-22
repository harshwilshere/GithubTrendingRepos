package com.harsh.githubtrendingrepos.ui


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harsh.githubtrendingrepos.data.GithubRepository
import com.harsh.githubtrendingrepos.data.model.Repo
import com.harsh.githubtrendingrepos.data.model.RepositoriesResponse
import com.harsh.githubtrendingrepos.util.Constants.Companion.QUERY_PAGE_SIZE
import com.harsh.githubtrendingrepos.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class TrendingReposViewModel @Inject constructor(
    private val repository: GithubRepository
): ViewModel() {

    val trendingRepos : MutableLiveData<Result<RepositoriesResponse>> = MutableLiveData()
    var trendingReposResponse: RepositoriesResponse? = null
    var repositoryPage = 1

    init {
        getTrendingRepos()
    }

    fun getTrendingRepos(searchQuery: String = "Java") = viewModelScope.launch {
        trendingRepos.postValue(Result.Loading())
        try {
            val response = repository.getTrendingRepos(searchQuery, repositoryPage, QUERY_PAGE_SIZE)
            trendingRepos.postValue(handleTrendingReposResponse(response))
        } catch (exception: Exception) {
            when(exception) {
                is IOException -> trendingRepos.postValue(Result.Error("Network Failure"))
                else -> trendingRepos.postValue(Result.Error("Conversion Failure"))
            }
        }
    }

    private fun handleTrendingReposResponse(response: Response<RepositoriesResponse>) : Result<RepositoriesResponse> {
        if(response.isSuccessful) {
            response.body()?.let { resultResponse ->
                repositoryPage++
                if(trendingReposResponse == null) {
                    trendingReposResponse = resultResponse
                } else {
                    val oldRepos = trendingReposResponse?.items
                    val newRepos = resultResponse.items
                    oldRepos?.addAll(newRepos)
                }
                return Result.Success(trendingReposResponse ?: resultResponse)
            }
        }
        return Result.Error(response.message())
    }

    public fun updateItem(repo : Repo) {
        val index = trendingReposResponse?.items?.indexOf(repo)
        if (index != null) {
            trendingReposResponse?.items?.set(index, repo)
            trendingReposResponse?.let {
                trendingRepos.postValue(Result.Success(it))
            }
        }

    }


}