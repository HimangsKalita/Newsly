package com.himangskalita.newsly.presentation.ui.fragments

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.himangskalita.newsly.R
import com.himangskalita.newsly.databinding.FragmentArticleBinding
import com.himangskalita.newsly.presentation.viewmodel.ArticleViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArticleFragment : Fragment() {

    private var _binding: FragmentArticleBinding? = null
    private val binding
        get() = _binding!!
    private val articleViewModel: ArticleViewModel by viewModels()

    private val args by navArgs<ArticleFragmentArgs>()
    private var onBackPressedCallback: OnBackPressedCallback? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentArticleBinding.inflate(inflater, container, false)

        binding.fgAtWvArticle.apply {

            settings.apply {
                userAgentString =
                    "Mozilla/5.0 (Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                databaseEnabled = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                safeBrowsingEnabled = true
            }

            CookieManager.getInstance().setAcceptCookie(true)
        }


        binding.fgAtWvArticle.webViewClient = object : WebViewClient() {


            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

                binding.apply {

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
                                findNavController().navigateUp()
//                                requireActivity().onBackPressedDispatcher.onBackPressed()
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



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val articleUrl = args.articleItem.url

        articleViewModel.checkArticleBookmarked(articleUrl)

        articleViewModel.isBookMarked.observe(viewLifecycleOwner) { isBookmarked ->

            updateBookmarkIcon(isBookmarked!!)

        }

        binding.fgAtFabBookmarkArticle.setOnClickListener {

            articleViewModel.toggleBookmark(args.articleItem)

//            if (articleViewModel.isBookMarked.value!!) {
//
//                articleViewModel.deleteBookmarkArticle(articleItem.url)
//                val color = ContextCompat.getColor(requireContext(), R.color.lightGrey)
//                binding.fgAtFabBookmarkArticle.backgroundTintList = ColorStateList.valueOf(color)
//                Snackbar.make(binding.root, "Bookmark Removed", Snackbar.LENGTH_SHORT)
//                    .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.white))
//                    .setAction("OK") {}
//                    .show()
//                Logger.d("Bookmark Added")
//
//            }else {
//
//                articleViewModel.addBookmarkArticle(articleItem)
//                val color = ContextCompat.getColor(requireContext(), R.color.blue)
//                binding.fgAtFabBookmarkArticle.backgroundTintList = ColorStateList.valueOf(color)
//                Snackbar.make(binding.root, "Bookmark Added", Snackbar.LENGTH_SHORT)
//                    .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.lightBlue))
//                    .setAction("UNDO") {
//
//                        articleViewModel.deleteBookmarkArticle(articleItem.url)
//                        val color = ContextCompat.getColor(requireContext(), R.color.lightGrey)
//                        binding.fgAtFabBookmarkArticle.backgroundTintList = ColorStateList.valueOf(color)
//                    }
//                    .show()
//                Logger.d("Bookmark Removed")
//            }
        }

        val webViewState = savedInstanceState?.getBundle("webViewState")

        webViewState?.let {

            binding.fgAtWvArticle.restoreState(it)
        } ?: run {

            binding.fgAtWvArticle.loadUrl(articleUrl)
        }
    }

    private fun updateBookmarkIcon(isBookmarked: Boolean) {

        if (isBookmarked) {

            val color = ContextCompat.getColor(requireContext(), R.color.blue)
            binding.fgAtFabBookmarkArticle.backgroundTintList = ColorStateList.valueOf(color)
        } else {

            val color = ContextCompat.getColor(requireContext(), R.color.lightGrey)
            binding.fgAtFabBookmarkArticle.backgroundTintList = ColorStateList.valueOf(color)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val webViewState = Bundle()
        binding.fgAtWvArticle.saveState(webViewState)
        outState.putBundle("webViewState", webViewState)
    }

//    override fun onResume() {
//        super.onResume()
//
//        articleViewModel.getWebViewStateValue()?.let {
//            binding.fgAtWvArticle.restoreState(it)
//        } ?: run {
//            binding.fgAtWvArticle.loadUrl(args.articleItem.url)
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//
//        val webViewState = Bundle()
//        binding.fgAtWvArticle.saveState(webViewState)
//
//        articleViewModel.saveWebViewState(webViewState)
//    }

    override fun onDestroy() {
        super.onDestroy()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
    }
}