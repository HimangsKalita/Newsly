package com.himangskalita.newsly.presentation.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.himangskalita.newsly.databinding.FragmentArticleBinding
//import com.himangskalita.newsly.presentation.viewmodel.ArticleViewModel
import com.himangskalita.newsly.presentation.viewmodel.BookmarkViewModel
import com.himangskalita.newsly.utils.Logger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArticleFragment : Fragment() {

    private var _binding: FragmentArticleBinding? = null
    private val binding
        get() = _binding!!
    private val bookmarkViewModel: BookmarkViewModel by viewModels()
//    private val articleViewModel: ArticleViewModel by viewModels()
    private val args by navArgs<ArticleFragmentArgs>()
    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.fgAtWvArticle.saveState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        savedInstanceState?.let {
            binding.fgAtWvArticle.restoreState(savedInstanceState)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentArticleBinding.inflate(inflater, container, false)

        val articleItem = args.articleItem

        binding.fgAtWvArticle.apply {

            settings.apply {
//                userAgentString =
//                    "Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.3 Mobile/15E148 Safari/604.1"
                javaScriptEnabled = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                safeBrowsingEnabled = true
            }
        }


        binding.fgAtWvArticle.webViewClient = object : WebViewClient() {


            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

                binding.apply {

                    fgAtTvError.visibility = View.GONE
                    fgAtBtnReload.visibility = View.GONE
                    fgAtPbLoading.visibility = View.VISIBLE
                    fgAtWvArticle.visibility = View.VISIBLE
                }

                onBackPressedCallback?.remove()

                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
                    object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {

                            if (view!!.canGoBack()) {

                                view.goBack()
                            } else {
                                isEnabled = false
                                remove()
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                        }
                    })
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                binding.apply {

                    fgAtPbLoading.visibility = View.GONE
                    fgAtWvArticle.visibility = View.VISIBLE
                }
            }
        }

        if (savedInstanceState == null) {

            binding.fgAtWvArticle.loadUrl(articleItem.url)
        }

        binding.fgAtFabBookmarkArticle.setOnClickListener {

            bookmarkViewModel.addBookmarkArticle(articleItem)
            Logger.d("Bookmark Added")
        }

        return binding.root
    }

    override fun onStop() {
        super.onStop()
//        articleViewModel.changeStateChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
    }
}