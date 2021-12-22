package com.harsh.githubtrendingrepos.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harsh.githubtrendingrepos.R
import com.harsh.githubtrendingrepos.databinding.ActivityTrendingReposBinding
import com.harsh.githubtrendingrepos.util.Constants
import com.harsh.githubtrendingrepos.util.Constants.Companion.KEY_SELECTION_POS
import com.harsh.githubtrendingrepos.util.Constants.Companion.KEY_SELECTION_URL
import com.harsh.githubtrendingrepos.util.Constants.Companion.QUERY_PAGE_SIZE
import com.harsh.githubtrendingrepos.util.Result
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrendingReposActivity : AppCompatActivity() {

    private lateinit var binding : ActivityTrendingReposBinding
    private val viewModel : TrendingReposViewModel by viewModels()
    private lateinit var repositoriesAdapter : RepositoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrendingReposBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView(savedInstanceState?.getString(KEY_SELECTION_URL), savedInstanceState?.getInt(KEY_SELECTION_POS))

        viewModel.trendingRepos.observe(this, { response ->
            when(response) {
                is Result.Success -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let {
                        repositoriesAdapter.differ.submitList(it.items.toList())
                        val totalPages = it.total / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.repositoryPage == totalPages
                        if(isLastPage) {
                            binding.recycler.setPadding(0, 0, 0, 0)
                        }
                    }
                }
                is Result.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(this@TrendingReposActivity, "An error occurred: $message", Toast.LENGTH_LONG).show()
                        showErrorMessage(message)
                    }
                }
                is Result.Loading -> {
                    showProgressBar()
                }
            }
        })

        binding.btnRetry.setOnClickListener {
            viewModel.getTrendingRepos()
        }

        repositoriesAdapter.setOnItemClickListener { repo, position ->
            repositoriesAdapter.notifyItemChanged(repositoriesAdapter.selectedItemPosition)
            repositoriesAdapter.selectedItemUrl = repo.url
            repositoriesAdapter.selectedItemPosition = position
            repositoriesAdapter.notifyItemChanged(position)
        }

    }

    private fun setupRecyclerView(selectedUrl: String?, selectedPosition: Int?) {
        repositoriesAdapter = RepositoriesAdapter()
        repositoriesAdapter.selectedItemUrl = selectedUrl
        repositoriesAdapter.selectedItemPosition = selectedPosition ?: RecyclerView.NO_POSITION
        repositoriesAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.recycler.apply {
            adapter = repositoriesAdapter
            layoutManager = LinearLayoutManager(this@TrendingReposActivity)
            addOnScrollListener(this@TrendingReposActivity.scrollListener)
        }
    }

    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private fun hideProgressBar() {
        binding.progress.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.progress.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideErrorMessage() {
        binding.btnRetry.visibility = View.INVISIBLE
        binding.error.visibility = View.INVISIBLE
        isError = false
    }

    private fun showErrorMessage(message: String) {
        binding.btnRetry.visibility = View.VISIBLE
        binding.error.visibility = View.VISIBLE
        binding.error.text = message
        isError = true
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoErrors = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate = isNoErrors && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling
            if(shouldPaginate) {
                viewModel.getTrendingRepos()
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_repos, menu)
        val search = menu.findItem(R.id.action_search)
        val searchView = search.actionView as SearchView
        searchView.queryHint = "Search"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                query?.let { searchQuery ->
                    repositoriesAdapter.differ.submitList(viewModel.trendingReposResponse?.items?.filter {
                        it.fullName.contains(searchQuery) || it.description?.contains(searchQuery) == true || it.name.contains(searchQuery)
                    })
                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SELECTION_URL, repositoriesAdapter.selectedItemUrl)
        outState.putInt(KEY_SELECTION_POS, repositoriesAdapter.selectedItemPosition)
    }
}