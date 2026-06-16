package com.rahulsah.studio.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.rahulsah.studio.data.model.*
import com.rahulsah.studio.ui.components.*
import com.rahulsah.studio.ui.theme.*

// ──────────────────────────────────────────────
// LIBRARY SCREEN
// ──────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onEditMedia: (Uri, MediaType) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Videos", "Photos", "Audio")

    // Sample library items (in production, load from MediaStore)
    val items = remember { sampleLibraryItems() }

    StudioBackground {
        Column(Modifier.fillMaxSize()) {
            StudioTopBar(
                title = "Library",
                subtitle = "Your downloaded media",
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Rounded.Search, null, tint = StudioTextSecondary)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Rounded.GridView, null, tint = StudioTextSecondary)
                    }
                }
            )

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = StudioViolet,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = StudioViolet, height = 2.dp
                    )
                }
            ) {
                tabs.forEachIndexed { idx, label ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = {
                            Text(label, style = MaterialTheme.typography.labelMedium.copy(
                                color = if (selectedTab == idx) StudioViolet else StudioTextHint
                            ))
                        }
                    )
                }
            }

            val filtered = when (selectedTab) {
                1 -> items.filter { it.mediaType == MediaType.VIDEO }
                2 -> items.filter { it.mediaType == MediaType.IMAGE }
                3 -> items.filter { it.mediaType == MediaType.AUDIO }
                else -> items
            }

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        Icons.Rounded.PhotoLibrary,
                        "No media yet",
                        "Downloaded files will appear here"
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered, key = { it.id }) { item ->
                        LibraryItemCard(
                            item = item,
                            onClick = { onEditMedia(Uri.parse(item.localPath), item.mediaType) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryItemCard(item: LibraryItem, onClick: () -> Unit) {
    Box(
        Modifier
            .aspectRatio(if (item.mediaType == MediaType.IMAGE) 1f else 16f / 9f)
            .background(StudioCard, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        if (item.thumbnailPath.isNotEmpty()) {
            AsyncImage(
                item.thumbnailPath, null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    when (item.mediaType) {
                        MediaType.AUDIO -> Icons.Rounded.MusicNote
                        MediaType.IMAGE -> Icons.Rounded.Image
                        else -> Icons.Rounded.VideoFile
                    },
                    null, Modifier.size(32.dp), tint = StudioViolet.copy(alpha = 0.5f)
                )
            }
        }

        // Overlay info bar
        Box(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(Color.Transparent, StudioBlack.copy(alpha = 0.85f))
                    )
                )
                .padding(8.dp)
        ) {
            Column {
                Text(
                    item.title,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.durationMs > 0) {
                        Text(
                            formatMs2(item.durationMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(0.7f)
                        )
                        Text("  ·  ", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.3f))
                    }
                    if (item.platform.isNotEmpty()) {
                        Text(item.platform, style = MaterialTheme.typography.labelSmall, color = StudioVioletGlow)
                    }
                }
            }
        }

        // Duration badge
        if (item.mediaType == MediaType.VIDEO && item.durationMs > 0) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(StudioBlack.copy(0.7f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(formatMs2(item.durationMs), style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }

        // Edit badge
        if (item.isEdited) {
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .background(StudioViolet, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("Edited", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color.White)
            }
        }
    }
}

private fun sampleLibraryItems(): List<LibraryItem> = listOf(
    LibraryItem("1", "YouTube Clip 1", "", "", MediaType.VIDEO, 62000, 18_000_000, platform = "YouTube"),
    LibraryItem("2", "Instagram Reel", "", "", MediaType.VIDEO, 30000, 8_500_000, platform = "Instagram", isEdited = true),
    LibraryItem("3", "Wallpaper", "", "", MediaType.IMAGE, 0, 2_400_000, platform = "Pinterest"),
    LibraryItem("4", "Podcast Episode", "", "", MediaType.AUDIO, 3600000, 52_000_000, platform = "SoundCloud"),
    LibraryItem("5", "TikTok Dance", "", "", MediaType.VIDEO, 15000, 4_200_000, platform = "TikTok"),
    LibraryItem("6", "Photo Shoot", "", "", MediaType.IMAGE, 0, 3_100_000),
)

private fun formatMs2(ms: Long): String {
    val total = ms / 1000
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

// ──────────────────────────────────────────────
// BROWSER SCREEN
// ──────────────────────────────────────────────
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    initialUrl: String = "https://www.google.com",
    onUrlDetected: (String) -> Unit,
    onBack: () -> Unit
) {
    var currentUrl by remember { mutableStateOf(initialUrl) }
    var addressBarText by remember { mutableStateOf(initialUrl) }
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0) }
    var canGoBack by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    val mediaUrlRegex = remember {
        Regex("(youtube\\.com/watch|youtu\\.be/|instagram\\.com/(p|reel|tv)|tiktok\\.com/@|twitter\\.com/.*/video|facebook\\.com/.*/video|vimeo\\.com/\\d|soundcloud\\.com/.*/|dailymotion\\.com/video)")
    }

    StudioBackground {
        Column(Modifier.fillMaxSize()) {
            // Browser top bar
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(StudioDeepNavy)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (canGoBack) webView?.goBack() else onBack() }) {
                    Icon(Icons.Rounded.ArrowBackIos, null, tint = if (canGoBack) StudioTextSecondary else StudioTextHint)
                }
                OutlinedTextField(
                    value = addressBarText,
                    onValueChange = { addressBarText = it },
                    modifier = Modifier.weight(1f).height(48.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = {
                        val url = if (!addressBarText.startsWith("http")) "https://$addressBarText" else addressBarText
                        webView?.loadUrl(url)
                        currentUrl = url
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioViolet,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = StudioTextPrimary,
                        unfocusedTextColor = StudioTextSecondary,
                        focusedContainerColor = StudioCard,
                        unfocusedContainerColor = StudioCard,
                        cursorColor = StudioViolet
                    ),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MaterialTheme.typography.bodySmall
                )
                // Download button (shows if current URL looks like a media page)
                if (mediaUrlRegex.containsMatchIn(currentUrl)) {
                    IconButton(onClick = { onUrlDetected(currentUrl) }) {
                        Icon(Icons.Rounded.Download, null, tint = StudioViolet)
                    }
                } else {
                    IconButton(onClick = { webView?.reload() }) {
                        Icon(Icons.Rounded.Refresh, null, tint = StudioTextHint)
                    }
                }
            }

            // Loading indicator
            if (isLoading) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = StudioViolet,
                    trackColor = Color.Transparent
                )
            }

            // Download hint banner
            if (mediaUrlRegex.containsMatchIn(currentUrl)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(StudioViolet.copy(alpha = 0.12f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Download, null, Modifier.size(16.dp), tint = StudioViolet)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Media detected — tap ↓ to download",
                        style = MaterialTheme.typography.labelMedium,
                        color = StudioVioletLight
                    )
                }
            }

            // WebView
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            mediaPlaybackRequiresUserGesture = false
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String) {
                                currentUrl = url
                                addressBarText = url
                                canGoBack = view.canGoBack()
                                isLoading = false
                            }
                            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                currentUrl = request.url.toString()
                                addressBarText = currentUrl
                                return false
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView, newProgress: Int) {
                                progress = newProgress
                                isLoading = newProgress < 100
                            }
                        }
                        loadUrl(initialUrl)
                        webView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
private fun Modifier.tabIndicatorOffset(tabPosition: TabPosition): Modifier = this

private fun formatMs(ms: Long): String {
    val total = ms / 1000
    val m = total / 60
    val s = total % 60
    return "%d:%02d".format(m, s)
}
