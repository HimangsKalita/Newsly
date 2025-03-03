package com.himangskalita.newsly.presentation.ui.activities

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.himangskalita.newsly.R
import com.himangskalita.newsly.databinding.ActivityMainBinding
import com.himangskalita.newsly.presentation.ui.fragments.BookmarkFragment
import com.himangskalita.newsly.presentation.ui.fragments.HeadlinesFragment
import com.himangskalita.newsly.presentation.ui.fragments.SearchFragment
import com.himangskalita.newsly.utils.Constants.Companion.APP_THEME_KEY
import com.himangskalita.newsly.utils.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import kotlin.time.measureTime

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navOptions: NavOptions
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    @Inject
    lateinit var preferenceDataStore: DataStore<Preferences>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val appTheme = runBlocking {
            preferenceDataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { preferences ->
                    preferences[intPreferencesKey(APP_THEME_KEY)]
                        ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                .first()
        }

        if (AppCompatDelegate.getDefaultNightMode() != appTheme) {
            AppCompatDelegate.setDefaultNightMode(appTheme)
        }

//        lifecycleScope.launch(Dispatchers.IO) {
//
//            setupAppTheme()
//        }

        binding = ActivityMainBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)


            insets
        }

        initialSetup()
    }

    private fun initialSetup() {

        setupJetpackNavigation()
        setupToolbar()
        setupBottomNavigation()
        setupKeyboardVisiblitiyListener()
    }

    private suspend fun setupAppTheme() {

//        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
//
//        val savedAppTheme = sharedPreferences.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
//        AppCompatDelegate.setDefaultNightMode(savedAppTheme)

        val timeTaken = measureTime {
            val savedAppTheme = withContext(Dispatchers.IO) {
                preferenceDataStore.data
                    .catch { exception ->
                        if (exception is IOException) {
                            emit(emptyPreferences())
                        } else {
                            throw exception
                        }
                    }
                    .map { preferences ->
                        preferences[intPreferencesKey(APP_THEME_KEY)]
                            ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                    .first()
            }

            if (AppCompatDelegate.getDefaultNightMode() != savedAppTheme) {
                withContext(Dispatchers.Main) {
                    AppCompatDelegate.setDefaultNightMode(savedAppTheme)
                }
            }

        }

        Logger.d("Time taken to load theme $timeTaken")
    }

    private fun setupJetpackNavigation() {

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment

        navController = navHostFragment.navController

        navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .setPopUpTo(
                navController.graph.startDestinationId,
                inclusive = false,
                saveState = true
            )
            .build()

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.fgHeadlines,
                R.id.fgSearch,
                R.id.fgBookmarks
            )
        )
    }

    private fun setupToolbar() {

        setSupportActionBar(findViewById(R.id.toolbar))
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.toolbar_menu, menu)
        val searchItem = menu?.findItem(R.id.action_search)

        navController.addOnDestinationChangedListener { _, destination, _ ->

            searchItem?.isVisible = destination.id == R.id.fgSearch
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {

        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    //    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
    //
    //        R.id.fgSettings -> {
    //
    //            navController.navigate(R.id.fgSettings, null, navOptions)
    //            true
    //        }
    //
    //        else -> super.onOptionsItemSelected(item)
    //    }

    private fun setupBottomNavigation() {


        binding.bnvMain.setupWithNavController(navController)

        binding.bnvMain.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.fgHeadlines -> {

                    if (navController.currentDestination?.id == R.id.fgArticle) {

                        navController.popBackStack(R.id.fgHeadlines, false)
                    }

                    val fragments = navHostFragment.childFragmentManager.fragments
                    val currentFragment = fragments.firstOrNull()

                    if (currentFragment is HeadlinesFragment) {

                        currentFragment.scrollToTop()

                    } else {

                        navController.navigate(R.id.fgHeadlines, null, navOptions)
                    }

                }

                R.id.fgSearch -> {

                    val fragments = navHostFragment.childFragmentManager.fragments
                    val currentFragment = fragments.firstOrNull()

                    if (currentFragment is SearchFragment) {

                        currentFragment.resetSearchFragment()

                    } else {

                        navController.navigate(R.id.fgSearch, null, navOptions)
                    }

                }

                R.id.fgBookmarks -> {

                    val fragments = navHostFragment.childFragmentManager.fragments
                    val currentFragment = fragments.firstOrNull()

                    if (currentFragment is BookmarkFragment) {

                        currentFragment.scrollToTop()

                    } else {

                        navController.navigate(R.id.fgBookmarks, null, navOptions)
                    }

                }
            }

            true
        }

    }

    private fun setupKeyboardVisiblitiyListener() {

        val rootView = findViewById<View>(android.R.id.content)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->

                val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())

                if (imeVisible) {

                    binding.bnvMain.visibility = View.GONE
                } else {

                    binding.bnvMain.visibility = View.VISIBLE
                }

                insets
            }

        } else {

            globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {

                val rect = Rect()

                rootView.getWindowVisibleDisplayFrame(rect)

                val screenHeight = rootView.height

                val keyboardHeight = screenHeight - rect.height()

                if (keyboardHeight > screenHeight * 0.15) {

                    binding.bnvMain.visibility = View.GONE
                } else {

                    binding.bnvMain.visibility = View.VISIBLE
                }
            }

            rootView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        val rootView = findViewById<View>(android.R.id.content)

        globalLayoutListener.let {

            rootView.viewTreeObserver.removeOnGlobalLayoutListener(it)
        }
        globalLayoutListener = null
    }
}