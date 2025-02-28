package com.himangskalita.newsly.presentation.ui.fragments

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.himangskalita.newsly.R
import com.himangskalita.newsly.databinding.FragmentArticleBinding
import com.himangskalita.newsly.presentation.ui.activities.MainActivity
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentArticleBinding.inflate(inflater, container, false)

        setupWebView()

        return binding.root
    }

    private fun setupWebView() {

        setupWebViewSettings()
        setupWebViewMethods()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebViewSettings() {

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

        setupWebViewDarkMode()
    }

    private fun setupWebViewDarkMode() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING) ) {

            WebSettingsCompat.setAlgorithmicDarkeningAllowed(binding.fgAtWvArticle.settings, true)

        }else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {

            val currentModeFlag = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {

                when (currentModeFlag) {

                    Configuration.UI_MODE_NIGHT_YES -> {

                        WebSettingsCompat.setForceDark(binding.fgAtWvArticle.settings, WebSettingsCompat.FORCE_DARK_ON)

                        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {

                            WebSettingsCompat.setForceDarkStrategy(binding.fgAtWvArticle.settings, WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING)
                        }
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {

                        WebSettingsCompat.setForceDark(binding.fgAtWvArticle.settings, WebSettingsCompat.FORCE_DARK_OFF)
                    }
                    else -> {

                        WebSettingsCompat.setForceDark(binding.fgAtWvArticle.settings, WebSettingsCompat.FORCE_DARK_AUTO)
                    }
                }
            }
        }

    }

    private fun setupWebViewMethods() {

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
                            }
                        }
                    })
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {

                val url = request?.url.toString()

                if (url.startsWith("http://")) {
                    val httpsUrl = url.replace("http://", "https://")
                    view?.loadUrl(httpsUrl)
                    return true
                }

                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                binding.apply {

                    fgAtPbLoading.visibility = View.GONE
                    fgAtWvArticle.visibility = View.VISIBLE
                }


            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val articleUrl = args.articleItem.url

        setupToolbarTitle()
        setupFAB(articleUrl)
        restoreWebViewState(savedInstanceState, articleUrl)
        setupWebViewScrolling()
    }

    private fun setupToolbarTitle() {

        (activity as? MainActivity)?.supportActionBar?.title = args.articleItem.title
    }

    private fun setupFAB(articleUrl: String) {

        articleViewModel.checkArticleBookmarked(articleUrl)

        articleViewModel.isBookMarked.observe(viewLifecycleOwner) { isBookmarked ->

            updateBookmarkIcon(isBookmarked!!)

        }

        binding.fgAtFabBookmarkArticle.setOnClickListener {

            articleViewModel.toggleBookmark(args.articleItem)

        }
    }

    private fun restoreWebViewState(savedInstanceState: Bundle?, articleUrl: String) {

        val webViewState = savedInstanceState?.getBundle("webViewState")

        webViewState?.let {

            binding.fgAtWvArticle.restoreState(it)
        } ?: run {

            binding.fgAtWvArticle.loadUrl(articleUrl)
        }
    }

    private fun setupWebViewScrolling() {

        var oldScrollY = 0

        binding.fgAtWvArticle.viewTreeObserver.addOnScrollChangedListener {

            val newScrollY = binding.fgAtWvArticle.scrollY

            if (newScrollY > oldScrollY) {

                binding.fgAtFabBookmarkArticle.hide()
            } else {

                binding.fgAtFabBookmarkArticle.show()
            }

            oldScrollY = newScrollY
        }
    }

    private fun updateBookmarkIcon(isBookmarked: Boolean) {

        if (isBookmarked) {

            binding.fgAtFabBookmarkArticle.setImageResource(R.drawable.bookmark_24px)

        } else {

            binding.fgAtFabBookmarkArticle.setImageResource(R.drawable.bookmark_outline_24px)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val webViewState = Bundle()
        binding.fgAtWvArticle.saveState(webViewState)
        outState.putBundle("webViewState", webViewState)
    }

    override fun onDestroy() {
        super.onDestroy()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
    }
}