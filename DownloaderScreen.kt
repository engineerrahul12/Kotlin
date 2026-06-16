package com.rahulsah.studio.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.rahulsah.studio.data.model.*
import com.rahulsah.studio.ui.components.*
import com.rahulsah.studio.ui.theme.*
import com.rahulsah.studio.viewmodel.DownloadTab
import com.rahulsah.studio.viewmodel.DownloadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloaderScreen(
    viewModel: DownloadViewModel,
    onOpenBrowser: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val downloads by viewModel.downloads.collectAsState()
    val clipboard = LocalClipboardManager.current

    StudioBackground {
        Column(Modifier.fillMaxSize()) {
            StudioTopBar(
                title = "Downloader",
                subtitle = "Download from any site",
                actions = {
                    IconButton(onClick = { onOpenBrowser("https://www.google.com") }) {
                        Icon(Icons.Rounded.Language, null, tint = StudioTextSecondary)
                    }
                }
            )

            // Tabs
            TabRow(
                selectedTabIndex = state.activeTab.ordinal,
                containerColor = Color.Transparent,
                contentColor = StudioViolet,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[state.activeTab.ordinal]),
                        color = StudioViolet,
                        height = 2.dp
                    )
                }
            ) {
                DownloadTab.entries.forEach { tab ->
                    Tab(
                        selected = state.activeTab == tab,
                        onClick = { viewModel.onTabSelected(tab) },
                        text = {
                            Text(
                                tab.name.lowercase().replaceFirstChar { it.uppercaseChar() },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (state.activeTab == tab) StudioViolet else StudioTextHint
                                )
                            )
                        }
                    )
                }
            }

            when (state.activeTab) {
                DownloadTab.DOWNLOADER -> DownloaderTab(
                    state = state,
                    onUrlChange = viewModel::onUrlChanged,
                    onAnalyze = { viewModel.analyzeUrl() },
                    onPaste = {
                        val text = clipboard.getText()?.text ?: ""
                        if (text.isNotBlank()) viewModel.onPaste(text)
                    },
                    onClear = { viewModel.onUrlChanged("") },
                )
                DownloadTab.BROWSER -> {
                    LaunchedEffect(Unit) { onOpenBrowser("https://www.google.com") }
                }
                DownloadTab.QUEUE -> QueueTab(
                    downloads = downloads.filter { it.status != DownloadStatus.COMPLETED },
                    onPause = viewModel::pauseDownload,
                    onResume = viewModel::resumeDownload,
                    onCancel = viewModel::cancelDownload,
                    onOpen = {}
                )
                DownloadTab.HISTORY -> QueueTab(
                    downloads = downloads.filter { it.status == DownloadStatus.COMPLETED },
                    onPause = {},
                    onResume = {},
                    onCancel = viewModel::removeDownload,
                    onOpen = {}
                )
            }
        }
    }

    // Quality bottom sheet
    if (state.showQualitySheet && state.mediaInfo != null) {
        QualityBottomSheet(
            mediaInfo = state.mediaInfo!!,
            selectedQuality = state.selectedQuality,
            onQualitySelect = viewModel::selectQuality,
            onDownload = viewModel::startDownload,
            onDismiss = viewModel::dismissQualitySheet
        )
    }

    // Error snackbar
    state.errorMessage?.let { err ->
        LaunchedEffect(err) {
            viewModel.clearError()
        }
        Snackbar(
            Modifier.padding(16.dp),
            containerColor = StudioCard,
            contentColor = StudioPink
        ) { Text(err) }
    }
}

@Composable
private fun DownloaderTab(
    state: com.rahulsah.studio.viewmodel.DownloadUiState,
    onUrlChange: (String) -> Unit,
    onAnalyze: () -> Unit,
    onPaste: () -> Unit,
    onClear: () -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // URL Input card
        item {
            GlassCard {
                Text("Paste URL", style = MaterialTheme.typography.labelMedium, color = StudioTextHint)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = state.urlInput,
                    onValueChange = onUrlChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("https://youtube.com/watch?v=...", color = StudioTextHint, style = MaterialTheme.typography.bodySmall)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { onAnalyze() }),
                    trailingIcon = {
                        if (state.urlInput.isNotEmpty()) {
                            IconButton(onClick = onClear) {
                                Icon(Icons.Rounded.Clear, null, tint = StudioTextHint)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioViolet,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = StudioTextPrimary,
                        unfocusedTextColor = StudioTextPrimary,
                        cursorColor = StudioViolet,
                        focusedContainerColor = StudioElevated,
                        unfocusedContainerColor = StudioElevated,
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onPaste,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, StudioBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioTextSecondary)
                    ) {
                        Icon(Icons.Rounded.ContentPaste, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Paste")
                    }
                    NeonButton(
                        text = "Analyze",
                        onClick = onAnalyze,
                        modifier = Modifier.weight(1f),
                        loading = state.isAnalyzing,
                        icon = Icons.Rounded.Search,
                        enabled = state.urlInput.isNotBlank()
                    )
                }
            }
        }

        // Supported platforms hint
        item {
            SupportedPlatformsHint()
        }

        // Error
        state.errorMessage?.let {
            item {
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.ErrorOutline, null, tint = StudioPink, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall, color = StudioPink)
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportedPlatformsHint() {
    Column {
        Text("Supported Platforms", style = MaterialTheme.typography.labelMedium, color = StudioTextHint,
            modifier = Modifier.padding(bottom = 10.dp))
        val platforms = listOf(
            "YouTube" to Color(0xFFFF0000), "Instagram" to Color(0xFFE1306C),
            "TikTok" to Color(0xFF69C9D0), "Twitter/X" to Color(0xFF1DA1F2),
            "Facebook" to Color(0xFF1877F2), "Vimeo" to Color(0xFF1AB7EA),
            "Reddit" to Color(0xFFFF4500), "SoundCloud" to Color(0xFFFF5500),
            "+ Any Site" to StudioViolet
        )
        val rows = platforms.chunked(3)
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (name, color) ->
                    Row(
                        Modifier
                            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(name, style = MaterialTheme.typography.labelSmall.copy(color = color))
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueTab(
    downloads: List<DownloadItem>,
    onPause: (String) -> Unit,
    onResume: (String) -> Unit,
    onCancel: (String) -> Unit,
    onOpen: (DownloadItem) -> Unit,
) {
    if (downloads.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(Icons.Rounded.Download, "Nothing here", "Downloads will appear here")
        }
    } else {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(downloads, key = { it.id }) { item ->
                DownloadItemCard(
                    item = item,
                    onPause = { onPause(item.id) },
                    onResume = { onResume(item.id) },
                    onCancel = { onCancel(item.id) },
                    onOpen = { onOpen(item) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QualityBottomSheet(
    mediaInfo: MediaInfo,
    selectedQuality: VideoQuality,
    onQualitySelect: (VideoQuality) -> Unit,
    onDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StudioDeepNavy,
        dragHandle = {
            Box(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.width(36.dp).height(4.dp).background(StudioBorder, RoundedCornerShape(2.dp)))
            }
        }
    ) {
        Column(Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            // Media preview header
            Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(64.dp).background(StudioCard, RoundedCornerShape(10.dp)).clip(RoundedCornerShape(10.dp))
                ) {
                    if (mediaInfo.thumbnailUrl.isNotEmpty()) {
                        AsyncImage(mediaInfo.thumbnailUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        mediaInfo.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = StudioTextPrimary
                    )
                    if (mediaInfo.author.isNotEmpty()) {
                        Text(mediaInfo.author, style = MaterialTheme.typography.bodySmall, color = StudioTextHint)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        mediaInfo.platform,
                        style = MaterialTheme.typography.labelSmall,
                        color = StudioVioletLight
                    )
                }
            }

            HorizontalDivider(color = StudioBorder, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            Text(
                "Select Quality",
                style = MaterialTheme.typography.titleSmall,
                color = StudioTextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Quality options
            mediaInfo.availableQualities.forEach { quality ->
                QualityOption(
                    quality = quality,
                    isSelected = selectedQuality == quality,
                    onClick = { onQualitySelect(quality) }
                )
            }

            Spacer(Modifier.height(12.dp))
            NeonButton(
                text = "Download ${selectedQuality.label}",
                onClick = onDownload,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                icon = Icons.Rounded.Download
            )
        }
    }
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
private fun Modifier.tabIndicatorOffset(tabPosition: TabPosition): Modifier = this
