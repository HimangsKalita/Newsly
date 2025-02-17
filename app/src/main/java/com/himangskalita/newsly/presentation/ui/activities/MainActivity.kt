package com.himangskalita.newsly.presentation.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.himangskalita.newsly.R
import com.himangskalita.newsly.databinding.ActivityMainBinding
import com.himangskalita.newsly.presentation.ui.fragments.BookmarkFragment
import com.himangskalita.newsly.presentation.ui.fragments.HeadlinesFragment
import com.himangskalita.newsly.presentation.ui.fragments.SearchFragment
import com.himangskalita.newsly.utils.Logger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

//    private val fragmentManager = supportFragmentManager
//    private var activeFragment: Fragment? = null
//
//    private val headlinesFragment: HeadlinesFragment by lazy { HeadlinesFragment() }
//    private val searchFragment: SearchFragment by lazy { SearchFragment() }
//    private val bookmarkFragment: BookmarkFragment by lazy { BookmarkFragment() }
//    private val settingsFragment: SettingsFragment by lazy { SettingsFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        setupFragments()
        setupBottomNavigation()
    }

//    private fun setupFragments() {
//
//        fragmentManager.beginTransaction()
//            .add(R.id.fragmentContainerView, settingsFragment, "settingsFragmentTag").hide(settingsFragment)
//            .add(R.id.fragmentContainerView, bookmarkFragment, "bookmarkFragmentTag").hide(bookmarkFragment)
//            .add(R.id.fragmentContainerView, searchFragment, "searchFragmentTag").hide(searchFragment)
//            .add(R.id.fragmentContainerView, headlinesFragment, "headlinesFragmentTag")
//            .commit()
//
//        activeFragment = headlinesFragment
//    }

    private fun setupBottomNavigation() {

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        binding.bnvMain.setupWithNavController(navController)

        val navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .setPopUpTo(navController.graph.startDestinationId, inclusive = false, saveState = true)
            .build()

        binding.bnvMain.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.fgHeadlines -> {

                    val fragments = navHostFragment.childFragmentManager.fragments
                    val currentFragment = fragments.firstOrNull()

//                    if (navController.currentDestination?.id == R.id.fgArticle) {
//
//                        Logger.d("In article going to home")
//
////                        navController.popBackStack(R.id.fgHeadlines, false)
////                        navController.navigateUp()
//                        navController.navigate(R.id.fgHeadlines, null, navOptions)
//
//                    } else
                        if (currentFragment is HeadlinesFragment) {

                        currentFragment.scrollToTop()

                    } else {

//                        val isArticleFragmentInBackstackResult = try {
//                            navController.getBackStackEntry(R.id.fgArticle)
//                        } catch (e: IllegalArgumentException) {
//                            null
//                        }
//
//                        val isArticleFragmentInBackstack =
//                            isArticleFragmentInBackstackResult != null
//
//                        if (isArticleFragmentInBackstack) {
////                            navController.popBackStack(R.id.fgArticle, false)
//                            navController.navigate(R.id.fgArticle, null, navOptions)
//                        } else {
////                            navController.popBackStack(R.id.fgHeadlines, false)
//                            navController.navigate(R.id.fgHeadlines, null, navOptions)
//                        }

                            navController.navigate(R.id.fgHeadlines, null, navOptions)

                    }

//                    switchFragment(headlinesFragment)

                }

                R.id.fgSearch -> {

                    val fragments = navHostFragment.childFragmentManager.fragments
                    val currentFragment = fragments.firstOrNull()

//                    if (navController.currentDestination?.id != R.id.fgSearch) {
//                        navController.navigate(R.id.fgSearch, null, singleTopNavOptions)
//                    }

                    if (currentFragment is SearchFragment) {

                        currentFragment.resetSearchFragment()

                    }else {

                        navController.navigate(R.id.fgSearch, null, navOptions)
                    }

//                    switchFragment(searchFragment)
                }

                R.id.fgBookmarks -> {

                    val fragments = navHostFragment.childFragmentManager.fragments
                    val currentFragment = fragments.firstOrNull()
//
//                    if (navController.currentDestination?.id != R.id.fgBookmarks) {
//                        navController.navigate(R.id.fgBookmarks, null, singleTopNavOptions)
//                    }

                    if (currentFragment is BookmarkFragment) {

                        currentFragment.scrollToTop()

                    }else {

                        navController.navigate(R.id.fgBookmarks, null, navOptions)
                    }

//                    switchFragment(bookmarkFragment)
                }

                R.id.fgSettings -> {

//                    if (navController.currentDestination?.id != R.id.fgSettings) {
//                        navController.navigate(R.id.fgSettings, null, singleTopNavOptions)
//                    }

                    navController.navigate(R.id.fgSettings, null, navOptions)


//                    switchFragment(settingsFragment)
                }
            }

            true
        }
    }

//    private fun switchFragment(fragment: Fragment) {
//
//        if (activeFragment != fragment) {
//
//            fragmentManager.beginTransaction()
//                .hide(activeFragment!!)
//                .show(fragment)
//                .commit()
//
//            activeFragment = fragment
//        }
//
//    }
}