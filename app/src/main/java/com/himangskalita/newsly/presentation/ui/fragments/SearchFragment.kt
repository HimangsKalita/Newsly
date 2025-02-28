package com.himangskalita.newsly.presentation.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.himangskalita.newsly.R
import com.himangskalita.newsly.data.models.Article
import com.himangskalita.newsly.databinding.FragmentSearchBinding
import com.himangskalita.newsly.presentation.adapter.SearchAdapter
import com.himangskalita.newsly.presentation.ui.activities.MainActivity
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

    //    private lateinit var searchItem: MenuItem
//    private lateinit var searchView: SearchView
//    private var menuProvider:MenuProvider? = null
    private var userScrolled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupSearchBar()
//        observeNetworkState()
        observeNewsSearchResult()

        return binding.root
    }

    private fun setupRecyclerView() {

        binding.fgSrRvNewsList.apply {

            layoutManager = LinearLayoutManager(requireContext())

            adapter = searchAdapter
        }

        setupSearchPagination()
    }

    private fun setupSearchPagination() {

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

//                        val searchQuery = searchView.query.toString().trim()
                        val searchQuery = binding.fgSrSvSearchViewNews.text.toString().trim()
                        if (searchQuery.isNotEmpty()) {

                            searchViewModel.searchNewsQueryPagination(searchQuery)
                        }
                    }
                }
            }
        })
    }


    private fun setupSearchBar() {

        binding.fgSrSvSearchBarNews.setOnClickListener {

            (activity as? MainActivity)?.supportActionBar?.hide()
            binding.fgSrSvSearchBarNews.visibility = View.GONE
            binding.fgSrSvSearchViewNews.show()
        }

        binding.fgSrSvSearchViewNews.editText.setOnEditorActionListener { textView, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                val query = textView.text.toString().trim()

                if (query.isNotEmpty()) {

                    searchNewsQuery(query)

                    binding.fgSrSvSearchBarNews.setText(query)

                    binding.fgSrSvSearchViewNews.hide()

                }

                return@setOnEditorActionListener true
            } else {

                return@setOnEditorActionListener false
            }
        }

        binding.fgSrSvSearchViewNews.editText.setOnFocusChangeListener { _, hasFocus ->

            if (!hasFocus) {
                binding.fgSrSvSearchBarNews.visibility = View.VISIBLE
                (activity as? MainActivity)?.supportActionBar?.show()
            }
        }
    }

    private fun searchNewsQuery(query: String) {

        searchViewModel.searchNewsQuery(query)
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
                    fgSrProgressBarNews.visibility = View.VISIBLE
                    fgSrRvNewsList.visibility = View.GONE
                    fgHlShimmerLayout.visibility = View.VISIBLE
                    fgHlShimmerLayout.startShimmer()
                }
            }

            is Resource.Success -> {

                binding.apply {

                    binding.fgSrPbPaginationSearchLoading.visibility = View.GONE
                    fgSrProgressBarNews.visibility = View.GONE
                    fgHlShimmerLayout.stopShimmer()
                    fgHlShimmerLayout.visibility = View.GONE
                    fgSrRvNewsList.visibility = View.VISIBLE
                }
                searchAdapter.submitList(result.data)
            }

            is Resource.Error -> {

                binding.apply {

                    binding.fgSrPbPaginationSearchLoading.visibility = View.GONE
                    fgSrProgressBarNews.visibility = View.GONE
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

                    } else {

                        binding.fgSrTvError.visibility = View.VISIBLE
                    }
                }

                Logger.d("Error fetching bookmarks: " + result.message.toString())
            }
        }
    }

//    private fun setupToolbarSearchView() {
//
//        val menuHost: MenuHost = requireActivity()
//
//        menuHost.addMenuProvider(object : MenuProvider {
//
//            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//
//                val searchItem = menu.findItem(R.id.action_search)
//                val searchView = searchItem.actionView as SearchView
//
//                searchItem.expandActionView()
//
//                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//
//                    override fun onQueryTextSubmit(query: String?): Boolean {
//
//                        if (!query.isNullOrEmpty()) {
//
//                            searchNewsQuery(query)
//                        }
//
//                        return true
//                    }
//
//                    override fun onQueryTextChange(newText: String?): Boolean {
//
//                        return true
//                    }
//                })
//            }
//
//            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
//
//                return true
//            }
//        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
//    }

    fun resetSearchFragment() {

        binding.root.hideKeyboard()
//        searchView.setQuery("", false)
//        searchItem.collapseActionView()
        binding.fgSrSvSearchBarNews.setText("")
        binding.fgSrSvSearchViewNews.setText("")
        binding.fgSrRvNewsList.visibility = View.GONE
    }

    private fun View.hideKeyboard() {

        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()

//        val menuHost = requireActivity()
//        menuProvider.let {
//
//            menuHost.removeMenuProvider(it!!)
//        }
//        menuProvider = null

        _binding = null
    }

}