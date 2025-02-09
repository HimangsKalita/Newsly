package com.himangskalita.newsly.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.himangskalita.newsly.R
import com.himangskalita.newsly.data.models.Article
import com.himangskalita.newsly.databinding.FragmentHeadlinesBinding
import com.himangskalita.newsly.presentation.adapter.NewsAdapter
import com.himangskalita.newsly.presentation.viewmodel.HeadlinesViewModel
import com.himangskalita.newsly.utils.Logger
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
    private val newsAdapter by lazy { NewsAdapter() }
    private var hasFetchedNews = false
    private var initialConnection = true

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

        binding.fhRvNewsList.apply {

            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter
        }
    }

    private fun observeNetworkStatus() {

        viewLifecycleOwner.lifecycleScope.launch {

            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {

                headlinesViewModel.connectivityStatus.collect { networkState ->

                    Logger.d("Network status: $networkState")

                    if (networkState) {

                        Logger.d("Internet connected")

                        if (!hasFetchedNews) {

                            handleConnectedState()
                        }

                        if (!initialConnection) {

                            showInternetConnectionStatus()
                        }else {

                            initialConnection = false
                        }

                    } else {

                        Logger.d("Internet disconnected")
                        handleDisconnectedState()
                    }
                }

            }
        }
    }

    private fun handleConnectedState() {

        hasFetchedNews = true
        fetchApiNewsHeadlines()
    }

    private fun showInternetConnectionStatus() {

        binding.fhTvInfo.apply {

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

        if (!initialConnection) {

            showInternetDisconnectionStatus()
        }else {

            initialConnection = false
        }
        if (binding.fhRvNewsList.size == 0) {

            fetchDatabaseHeadlines()
        }
    }

    private fun showInternetDisconnectionStatus() {

        binding.fhTvInfo.apply {

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

                    fhPbLoading.visibility = View.VISIBLE
                    fhShimmerLayout.visibility = View.VISIBLE
                    fhShimmerLayout.startShimmer()
                }
            }

            is Resource.Success -> {

                binding.apply {

                    fhPbLoading.visibility = View.GONE
                    fhShimmerLayout.stopShimmer()
                    fhShimmerLayout.visibility = View.GONE
                }
                newsAdapter.submitList(result.data)
            }

            is Resource.Error -> {

                binding.apply {

                    fhPbLoading.visibility = View.GONE
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

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}