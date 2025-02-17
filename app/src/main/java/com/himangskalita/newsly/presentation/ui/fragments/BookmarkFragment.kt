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
import com.himangskalita.newsly.databinding.FragmentBookmarksBinding
import com.himangskalita.newsly.presentation.adapter.BookmarkAdapter
import com.himangskalita.newsly.presentation.viewmodel.BookmarkViewModel
import com.himangskalita.newsly.utils.Logger
import com.himangskalita.newsly.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookmarkFragment : Fragment() {

    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!
    private val bookmarkViewModel: BookmarkViewModel by viewModels()
    private val bookmarkAdapter by lazy { BookmarkAdapter{ article ->

        val action = BookmarkFragmentDirections.actionFgBookmarksToFgArticle(article)
        findNavController().navigate(action)
    } }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentBookmarksBinding.inflate(inflater, container, false)

        setupBookmarkRecycleView()
        observeDatabaseResult()
        getDatabaseBookmarks()

        return binding.root
    }

    private fun setupBookmarkRecycleView() {

        binding.fgBmRvBookmarkList.apply {

            layoutManager = LinearLayoutManager(requireContext())
            adapter = bookmarkAdapter
        }
    }

    private fun observeDatabaseResult() {

        viewLifecycleOwner.lifecycleScope.launch {

            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                bookmarkViewModel.bookmarks.collect { bookmarkList ->

                    when (bookmarkList) {

                        is Resource.Ini -> {}
                        is Resource.Loading -> {

                            binding.apply {

                                binding.fgBmTvEmpty.visibility = View.GONE
                                fgBmPbLoading.visibility = View.VISIBLE
                                fgBmRvBookmarkList.visibility = View.GONE
                                fgBmShimmerLayout.visibility = View.VISIBLE
                                fgBmShimmerLayout.startShimmer()

                            }
                        }

                        is Resource.Success -> {

                            binding.apply {

                                binding.fgBmTvEmpty.visibility = View.GONE
                                fgBmPbLoading.visibility = View.GONE
                                fgBmShimmerLayout.stopShimmer()
                                fgBmShimmerLayout.visibility = View.GONE
                                fgBmRvBookmarkList.visibility = View.VISIBLE
                            }

                            bookmarkAdapter.submitList(bookmarkList.data)
                        }

                        is Resource.Error -> {

                            binding.apply {

                                fgBmPbLoading.visibility = View.GONE
                                fgBmShimmerLayout.stopShimmer()
                                fgBmShimmerLayout.visibility = View.GONE
                            }

                            val firstWord = bookmarkList.message?.trim()?.substringBefore(" ") ?: ""

                            if (firstWord.equals("empty", ignoreCase = true)) {

                                binding.fgBmTvEmpty.visibility = View.VISIBLE
                                binding.fgBmRvBookmarkList.visibility = View.GONE
                            }else {

                                binding.fgBmRvBookmarkList.visibility = View.VISIBLE
                            }

                            Logger.d("Error fetching bookmarks: " + bookmarkList.message.toString())
                        }
                    }
                }
            }
        }
    }

    private fun getDatabaseBookmarks() {

        bookmarkViewModel.getBookmarkArticlesList()
    }

    fun scrollToTop() {

        binding.fgBmRvBookmarkList.smoothScrollToPosition(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}