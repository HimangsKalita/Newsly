package com.himangskalita.newsly.presentation.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.himangskalita.newsly.R
import com.himangskalita.newsly.data.models.Article
import com.himangskalita.newsly.databinding.FragmentSearchBinding
import com.himangskalita.newsly.presentation.adapter.SearchAdapter
import com.himangskalita.newsly.presentation.viewmodel.SearchViewModel
import com.himangskalita.newsly.utils.Logger
import com.himangskalita.newsly.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val searchViewModel: SearchViewModel by viewModels()
    private val searchAdapter by lazy {
        SearchAdapter { articleItem ->

            val action = SearchFragmentDirections.actionFgmSearchToFgArticle(articleItem)
            findNavController().navigate(action)
        }
    }
    private var userScrolled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        setupRecyclerView()
//        observeNetworkState()
        handleSearchQuery()
        observeNewsSearchResult()

        return binding.root
    }

    private fun setupRecyclerView() {

        binding.fgSrRvNewsList.apply {

            layoutManager = LinearLayoutManager(requireContext())

            adapter = searchAdapter
        }

        binding.fgSrRvNewsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {

                    userScrolled = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                val layoutManager = binding.fgSrRvNewsList.layoutManager as LinearLayoutManager

                if (dy > 1) {

                    val visibleItemCount = layoutManager.childCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    Logger.d("visibleItemCount: $visibleItemCount\nfirstVisibleItemPosition: $firstVisibleItemPosition\ntotalItemCount: $totalItemCount")

                    if (userScrolled && firstVisibleItemPosition + visibleItemCount >= totalItemCount) {

                        Logger.d("Reached end of list")

                        Logger.d("Requesting search pagination")

                        val searchQuery = binding.fgSrSvSearchNews.query?.toString() ?: ""
                        if (searchQuery.isNotEmpty()) {

                            searchViewModel.searchNewsQueryPagination(binding.fgSrSvSearchNews.query.toString())
                        }
                    }
                }
            }
        })
    }

//    private fun observeNetworkState() {
//
//        viewLifecycleOwner.lifecycleScope.launch {
//
//            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
//
//                searchViewModel.networkState.collect { networkUIState ->
//
//                    when (networkUIState) {
//
//                        is NetworkUIState.Unknown -> {
//                            Logger.d("Network State: Unknown")
//                        }
//
//                        is NetworkUIState.Connected -> {
//
////                            showInternetConnectionStatus()
//
//                            if (!headlinesViewModel.firstConnection.value!!) {
//
//                                Logger.d("FirstConnection - Connected(Showing): ${headlinesViewModel.firstConnection.value}")
//                                showInternetConnectionStatus()
//                            } else {
//
//                                Logger.d("FirstConnection - Connected(Not showing - Changing values): ${headlinesViewModel.firstConnection.value}")
//                                headlinesViewModel.changeFirstConnectionValue()
//                            }
//
//                            if (!headlinesViewModel.hasFetchedNews) {
//
//                                Logger.d("Hasfetched news: ${headlinesViewModel.hasFetchedNews}")
//                                fetchApiNewsHeadlines()
//                                headlinesViewModel.changeHasFetchedNews()
//                                Logger.d("Hasfetched news: ${headlinesViewModel.hasFetchedNews}")
//                            }else {
//
//                                Logger.d("Hasfetched news: ${headlinesViewModel.hasFetchedNews}")
//                            }
//                        }
//
//                        is NetworkUIState.Disconnected -> {
//
//                            handleDisconnectedState()
//                        }
//                    }
//                }
//            }
//        }
//    }

    private fun handleSearchQuery() {

        binding.fgSrSvSearchNews.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {

                searchNewsQuery(query)
                binding.root.hideKeyboard()

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {


                return true
            }
        })
    }

    private fun observeNewsSearchResult() {

        viewLifecycleOwner.lifecycleScope.launch {

            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                searchViewModel.result.collectLatest { result ->

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

            is Resource.SwipeLoading -> {}

            is Resource.PaginationLoading -> {

                binding.fgSrPbPaginationSearchLoading.visibility = View.VISIBLE
            }

            is Resource.Loading -> {

                binding.apply {

                    binding.fgSrTvEmpty.visibility = View.GONE
                    binding.fgSrTvError.visibility = View.GONE
                    fgSrPbSearchingNews.visibility = View.VISIBLE
                    fgSrRvNewsList.visibility = View.GONE
                    fgHlShimmerLayout.visibility = View.VISIBLE
                    fgHlShimmerLayout.startShimmer()
                }
            }

            is Resource.Success -> {

                binding.apply {

                    binding.fgSrPbPaginationSearchLoading.visibility = View.GONE
                    fgSrPbSearchingNews.visibility = View.GONE
                    fgHlShimmerLayout.stopShimmer()
                    fgHlShimmerLayout.visibility = View.GONE
                    fgSrRvNewsList.visibility = View.VISIBLE
                }
                searchAdapter.submitList(result.data)
            }

            is Resource.Error -> {

                binding.apply {

                    binding.fgSrPbPaginationSearchLoading.visibility = View.GONE
                    fgSrPbSearchingNews.visibility = View.GONE
                    fgHlShimmerLayout.stopShimmer()
                    fgHlShimmerLayout.visibility = View.GONE
//                    fgSrRvNewsList.visibility = View.VISIBLE
                }

                val firstWord = result.message?.trim()?.substringBefore(" ") ?: ""

                if (firstWord.equals("no", ignoreCase = true)) {

                    binding.fgSrTvEmpty.visibility = View.VISIBLE
                    binding.fgSrRvNewsList.visibility = View.GONE
                } else {

                    if (searchAdapter.itemCount > 0) {

                        Snackbar.make(binding.root, "Error fetching news", Snackbar.LENGTH_SHORT).let { snackbar ->
                            snackbar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.lightBlue))
                            snackbar.setAction("OK") {
                                snackbar.dismiss()
                                Logger.d("OK clicked!")
                            }
                            snackbar.show()
                        }

                    } else {

                        binding.fgSrTvError.visibility = View.VISIBLE
                    }
                }

                Logger.d("Error fetching bookmarks: " + result.message.toString())
            }
        }
    }

    private fun searchNewsQuery(query: String?) {

        if (!query.isNullOrEmpty()) {


            searchViewModel.searchNewsQuery(query)
        }
    }

    fun resetSearchFragment() {

        binding.root.hideKeyboard()
        binding.fgSrSvSearchNews.setQuery("", false)
        binding.fgSrRvNewsList.visibility = View.GONE
    }

    private fun View.hideKeyboard() {

        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}