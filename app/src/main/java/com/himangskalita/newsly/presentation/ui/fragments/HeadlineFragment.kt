package com.himangskalita.newsly.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.himangskalita.newsly.R
import com.himangskalita.newsly.data.models.Article
import com.himangskalita.newsly.databinding.FragmentHeadlinesBinding
import com.himangskalita.newsly.presentation.adapter.NewsAdapter
import com.himangskalita.newsly.presentation.viewmodel.HeadlinesViewModel
import com.himangskalita.newsly.utils.Logger
import com.himangskalita.newsly.utils.NetworkUIState
import com.himangskalita.newsly.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class HeadlinesFragment : Fragment() {

    private var _binding: FragmentHeadlinesBinding? = null
    private val binding get() = _binding!!
    private val headlinesViewModel: HeadlinesViewModel by viewModels()
    private val newsAdapter by lazy {
        NewsAdapter { articleItem ->

            val action = HeadlinesFragmentDirections.actionFgHeadlinesToFgArticle(articleItem)
            findNavController().navigate(action)
        }
    }
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var userScrolled = false
    private val queryParams = mapOf("category" to "technology")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHeadlinesBinding.inflate(inflater, container, false)

        swipeRefreshLayout = binding.fgHlSrlRefresh

        setupRecycleView()
        observeNetworkStatus()
        observeNewsResult()

        swipeRefreshLayout.setOnRefreshListener {

            headlinesViewModel.changeSwipeRefreshLoadingTrue()
            fetchApiNewsHeadlines()
        }

        return binding.root
    }

    private fun setupRecycleView() {

        binding.fgHlRvNewsList.apply {

            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter
        }

        binding.fgHlRvNewsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {

                    userScrolled = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                val layoutManager = binding.fgHlRvNewsList.layoutManager as LinearLayoutManager

                if (dy > 1) {

                    val visibleItemCount = layoutManager.childCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    Logger.d("visibleItemCount: $visibleItemCount\nfirstVisibleItemPosition: $firstVisibleItemPosition\ntotalItemCount: $totalItemCount")

                    if (userScrolled && firstVisibleItemPosition + visibleItemCount >= totalItemCount) {

                        Logger.d("Reached end of list")

                        Logger.d("Requesting pagination")
                        headlinesViewModel.fetchApiHeadlinesPagination(queryParams)
                    }
                }
            }
        })
    }

    private fun observeNetworkStatus() {

        viewLifecycleOwner.lifecycleScope.launch {

            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                headlinesViewModel.networkUIState.collect { networkUIState ->

                    when (networkUIState) {

                        is NetworkUIState.Unknown -> {
                            Logger.d("Network State: Unknown")
                        }

                        is NetworkUIState.Connected -> {

//                            showInternetConnectionStatus()

                            if (!headlinesViewModel.firstConnection.value!!) {

                                Logger.d("FirstConnection - Connected(Showing): ${headlinesViewModel.firstConnection.value}")
                                showInternetConnectionStatus()
                            } else {

                                Logger.d("FirstConnection - Connected(Not showing - Changing values): ${headlinesViewModel.firstConnection.value}")
                                headlinesViewModel.changeFirstConnectionValue()
                            }

                            if (!headlinesViewModel.hasFetchedNews) {

                                Logger.d("Hasfetched news: ${headlinesViewModel.hasFetchedNews}")
                                fetchApiNewsHeadlines()
                                headlinesViewModel.changeHasFetchedNewsTrue()
                                Logger.d("Hasfetched news: ${headlinesViewModel.hasFetchedNews}")
                            } else {

                                Logger.d("Hasfetched news: ${headlinesViewModel.hasFetchedNews}")
                            }
                        }

                        is NetworkUIState.Disconnected -> {

                            handleDisconnectedState()
                        }
                    }
                }

            }
        }
    }

    private fun showInternetConnectionStatus() {

        binding.fgHlTvInfo.apply {

            setBackgroundColor(requireContext().getColor(R.color.green))
            text = "internet connected"
            visibility = View.VISIBLE

            lifecycleScope.launch {

                delay(1000)
                withContext(Dispatchers.Main) {

                    visibility = View.GONE
                }
            }
        }
    }

    private fun handleDisconnectedState() {

        showInternetDisconnectionStatus()
        headlinesViewModel.changeFirstConnectionValue()

        if (!headlinesViewModel.hasFetchedNews) {

//            if (!headlinesViewModel.firstConnection.value!!) {
//
//                Logger.d("hasFetchedNews - Disconnected(showing): ${headlinesViewModel.firstConnection.value}")
//                showInternetDisconnectionStatus()
//
//            } else {
//
//                Logger.d("FirstConnection - Disconnected(Not showing - Changing values): ${headlinesViewModel.firstConnection.value}")
//                headlinesViewModel.changeFirstConnectionValue()
//                Logger.d("FirstConnection - Disconnected(Not showing - Changed values): ${headlinesViewModel.firstConnection.value}")
//            }
            fetchDatabaseHeadlines()

        }
    }

    private fun showInternetDisconnectionStatus() {

        binding.fgHlTvInfo.apply {

            setBackgroundColor(requireContext().getColor(R.color.errorRed))
            text = "no internet connection"
            visibility = View.VISIBLE

        }
    }

    private fun observeNewsResult() {

        viewLifecycleOwner.lifecycleScope.launch {

            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                headlinesViewModel.result.collectLatest { result ->

                    handleResult(result)
                }
            }
        }
    }

    private fun handleResult(result: Resource<List<Article>>) {

        when (result) {

            is Resource.Ini -> {
                Logger.d("Initial state of viewmodel")
            }

            is Resource.Loading -> {

                binding.apply {

                    fgHlPbLoading.visibility = View.VISIBLE
                    fgHlRvNewsList.visibility = View.GONE
                    fgHlShimmerLayout.visibility = View.VISIBLE
                    fgHlShimmerLayout.startShimmer()
                }
            }

            is Resource.SwipeLoading -> {

                binding.apply {

                    fgHlRvNewsList.visibility = View.GONE
                    fgHlShimmerLayout.visibility = View.VISIBLE
                    fgHlShimmerLayout.startShimmer()
                }
            }

            is Resource.PaginationLoading -> {

                binding.apply {

                    fgHlPbPaginationLoading.visibility = View.VISIBLE
                }
            }

            is Resource.Success -> {

                headlinesViewModel.changeSwipeRefreshLoadingFalse()

                binding.apply {

                    fgHlPbPaginationLoading.visibility = View.GONE
                    fgHlSrlRefresh.isRefreshing = false
                    fgHlPbLoading.visibility = View.GONE
                    fgHlShimmerLayout.stopShimmer()
                    fgHlShimmerLayout.visibility = View.GONE
                    fgHlRvNewsList.visibility = View.VISIBLE
                }
                newsAdapter.submitList(result.data)
            }

            is Resource.Error -> {

                headlinesViewModel.changeSwipeRefreshLoadingFalse()

                binding.apply {

                    fgHlPbPaginationLoading.visibility = View.GONE
                    fgHlSrlRefresh.isRefreshing = false
                    fgHlPbLoading.visibility = View.GONE
                    fgHlRvNewsList.visibility = View.VISIBLE
                }

                if (newsAdapter.itemCount > 0) {

                    binding.apply {

                        fgHlShimmerLayout.stopShimmer()
                        fgHlShimmerLayout.visibility = View.GONE
                    }

                    Snackbar.make(binding.root, "Error fetching news", Snackbar.LENGTH_SHORT)
                        .let { snackbar ->
                            snackbar.setActionTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.lightBlue
                                )
                            )
                            snackbar.setAction("OK") {
                                snackbar.dismiss()
                                Logger.d("OK clicked!")
                            }
                            snackbar.show()
                        }
                }

                Logger.d("Error fetching news: " + result.message.toString())
            }

        }
    }

    private fun fetchApiNewsHeadlines() {

        Logger.d("Api News Fetch Request")
        headlinesViewModel.fetchApiHeadlines(queryParams)
    }

    private fun fetchDatabaseHeadlines() {

        Logger.d("Database News Fetch Request")
        headlinesViewModel.fetchDatabaseHeadlines()
    }

    fun scrollToTop() {

        binding.fgHlRvNewsList.smoothScrollToPosition(0)
    }

    override fun onStop() {
        super.onStop()
        headlinesViewModel.changeFirstConnectionValueTrue()
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}