package com.rahulsah.studio

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.rahulsah.studio.data.model.MediaType
import com.rahulsah.studio.ui.Screen
import com.rahulsah.studio.ui.components.StudioBottomNav
import com.rahulsah.studio.ui.screens.*
import com.rahulsah.studio.ui.theme.StudioTheme
import com.rahulsah.studio.viewmodel.DownloadViewModel
import com.rahulsah.studio.viewmodel.EditorViewModel
import java.net.URLDecoder
import java.net.URLEncoder

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle share intent (URL from another app)
        val sharedUrl = intent?.takeIf { it.action == Intent.ACTION_SEND }
            ?.getStringExtra(Intent.EXTRA_TEXT)

        setContent {
            StudioTheme {
                StudioApp(initialSharedUrl = sharedUrl)
            }
        }
    }
}

@Composable
fun StudioApp(initialSharedUrl: String? = null) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    val downloadViewModel: DownloadViewModel = viewModel()
    val editorViewModel: EditorViewModel = viewModel()

    // Handle shared URL on first launch
    LaunchedEffect(initialSharedUrl) {
        initialSharedUrl?.let { url ->
            navController.navigate("downloader")
            downloadViewModel.onPaste(url)
        }
    }

    val showBottomBar = currentRoute in listOf("home", "downloader", "library", "editor")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                StudioBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateDownload = { navController.navigate("downloader") },
                    onNavigateLibrary  = { navController.navigate("library") },
                    onNavigateEditor   = { navController.navigate("editor") },
                    onNavigateBrowser  = { url ->
                        navController.navigate(Screen.Browser.createRoute(url))
                    }
                )
            }

            composable(Screen.Downloader.route) {
                DownloaderScreen(
                    viewModel = downloadViewModel,
                    onOpenBrowser = { url ->
                        navController.navigate(Screen.Browser.createRoute(url))
                    }
                )
            }

            composable(
                route = Screen.Browser.route,
                arguments = listOf(navArgument("url") { type = NavType.StringType })
            ) { entry ->
                val rawUrl = entry.arguments?.getString("url") ?: "https://www.google.com"
                val decodedUrl = URLDecoder.decode(rawUrl, "UTF-8")
                BrowserScreen(
                    initialUrl = decodedUrl,
                    onUrlDetected = { url ->
                        downloadViewModel.onPaste(url)
                        navController.navigate("downloader") {
                            popUpTo("downloader") { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Library.route) {
                LibraryScreen(
                    onEditMedia = { uri, type ->
                        editorViewModel.loadMedia(uri, type)
                        navController.navigate(
                            Screen.Editor.createRoute(uri.toString(), type.name)
                        )
                    }
                )
            }

            composable(
                route = Screen.Editor.route,
                arguments = listOf(
                    navArgument("uri")  { type = NavType.StringType; defaultValue = "" },
                    navArgument("type") { type = NavType.StringType; defaultValue = "VIDEO" }
                )
            ) { entry ->
                val uriStr = entry.arguments?.getString("uri") ?: ""
                val typeStr = entry.arguments?.getString("type") ?: "VIDEO"
                LaunchedEffect(uriStr) {
                    if (uriStr.isNotBlank()) {
                        val decoded = URLDecoder.decode(uriStr, "UTF-8")
                        val mediaType = runCatching { MediaType.valueOf(typeStr) }.getOrDefault(MediaType.VIDEO)
                        editorViewModel.loadMedia(Uri.parse(decoded), mediaType)
                    }
                }
                EditorScreen(
                    viewModel = editorViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
