package com.himangskalita.newsly.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
    private val newsAdapter by lazy { NewsAdapter{articleItem ->

        val action = HeadlinesFragmentDirections.actionFgHeadlinesToFgArticle(articleItem)
        findNavController().navigate(action)
    } }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHeadlinesBinding.inflate(inflater, container, false)

        setupRecycleView()
        observeNetworkStatus()
        observeNewsResult()

        return binding.root
    }

    private fun setupRecycleView() {

        binding.fgHlRvNewsList.apply {

            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter
        }
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
                                headlinesViewModel.changeHasFetchedNews()
                                Logger.d("Hasfetched news: ${headlinesViewModel.hasFetchedNews}")
                            }else {

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

                delay(1500)
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

            is Resource.Success -> {

                binding.apply {

                    fgHlPbLoading.visibility = View.GONE
                    fgHlShimmerLayout.stopShimmer()
                    fgHlShimmerLayout.visibility = View.GONE
                    fgHlRvNewsList.visibility = View.VISIBLE
                }
                newsAdapter.submitList(result.data)
            }

            is Resource.Error -> {

                binding.apply {

                    fgHlPbLoading.visibility = View.GONE
                    fgHlRvNewsList.visibility = View.VISIBLE
                }
                Logger.d("Error fetching news: " + result.message.toString())
            }
        }
    }

    private fun fetchApiNewsHeadlines() {

        val queryParams = mapOf("category" to "technology")

        Logger.d("Api News Fetch Request")
        headlinesViewModel.fetchApiHeadlines(queryParams)
    }

    private fun fetchDatabaseHeadlines() {

        Logger.d("Database News Fetch Request")
        headlinesViewModel.fetchDatabaseHeadlines()
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